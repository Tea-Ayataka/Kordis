package net.ayataka.kordis.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.neovisionaries.ws.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.GatewayIntent
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.utils.*
import net.ayataka.kordis.websocket.handlers.channel.ChannelCreateHandler
import net.ayataka.kordis.websocket.handlers.channel.ChannelDeleteHandler
import net.ayataka.kordis.websocket.handlers.channel.ChannelUpdateHandler
import net.ayataka.kordis.websocket.handlers.guild.*
import net.ayataka.kordis.websocket.handlers.message.*
import net.ayataka.kordis.websocket.handlers.other.ReadyHandler
import net.ayataka.kordis.websocket.handlers.other.TypingStartHandler
import net.ayataka.kordis.websocket.handlers.other.UserUpdateHandler
import net.ayataka.kordis.websocket.handlers.voice.VoiceServerUpdateHandler
import net.ayataka.kordis.websocket.handlers.voice.VoiceStateUpdateHandler
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("EXPERIMENTAL_API_USAGE")
class GatewayClient(
        private val client: DiscordClientImpl,
        private val endpoint: String,
        private val intents: Set<GatewayIntent>
) : CoroutineScope, WebSocketAdapter() {
    override val coroutineContext = newSingleThreadContext("Gateway Packet Handler")

    @Volatile private var ready: Boolean = false
    @Volatile private var websocket: WebSocket? = null
    @Volatile private var sessionId: String? = null
    @Volatile private var lastSequence: Int? = null
    @Volatile private var heartbeatAckReceived: Boolean = false
    @Volatile private var heartbeatTask: Job? = null
    @Volatile private var activity: JsonObject? = null

    private val mutex = Mutex()
    private val gson = Gson()
    private val sendChannel = Channel<String>(Channel.UNLIMITED)
    private val memberRequestChannel = Channel<Long>(Channel.UNLIMITED)
    private val rateLimiter = RateLimiter(60 * 1000, 100) // The actual limit is 120

    private val handlers = listOf(
            ChannelCreateHandler(),
            ChannelDeleteHandler(),
            ChannelUpdateHandler(),
            GuildBanAddHandler(),
            GuildBanRemoveHandler(),
            GuildCreateHandler(),
            GuildDeleteHandler(),
            GuildEmojisUpdateHandler(),
            GuildMemberAddHandler(),
            GuildMemberRemoveHandler(),
            GuildMemberUpdateHandler(),
            GuildMembersChunkHandler(),
            GuildRoleCreateHandler(),
            GuildRoleDeleteHandler(),
            GuildRoleUpdateHandler(),
            GuildUpdateHandler(),
            MessageCreateHandler(),
            MessageDeleteHandler(),
            MessageDeleteBulkHandler(),
            MessageReactionAddHandler(),
            MessageReactionRemoveHandler(),
            MessageReactionRemoveAllHandler(),
            MessageUpdateHandler(),
            PresenseUpdateHandler(),
            ReadyHandler(),
            TypingStartHandler(),
            UserUpdateHandler(),
            VoiceServerUpdateHandler(),
            VoiceStateUpdateHandler()
    )

    init {
        launch(newSingleThreadContext("Gateway Packet Dispatcher")) {
            for (packet in sendChannel) {
                try {
                    // Dispose the packet if the websocket is not opened
                    if (websocket?.isOpen != true) {
                        continue
                    }

                    // Wait for the 'cool-down' and the 'ready' state
                    while (!ready || rateLimiter.isLimited()) {
                        delay(10)
                    }

                    // Send it
                    websocket?.sendText(packet)
                    rateLimiter.increment()

                    LOGGER.trace("Sent: $packet")
                } catch (ex: Exception) {
                    LOGGER.error("WebSocket Error", ex)
                }
            }
        }

        timer(interval = 1000, context = newSingleThreadContext("Gateway Server Member Requester")) {
            val servers = memberRequestChannel.receiveAll()
            if (servers.isEmpty()) {
                return@timer
            }

            queue(Opcode.REQUEST_GUILD_MEMBERS, json {
                "guild_id" to jsonArray { servers.forEach { +JsonPrimitive(it) } }
                "query" to ""
                "limit" to 0
            })
        }
    }

    suspend fun connect(): Unit = mutex.withLock {
        val state = websocket?.state
        if (state == WebSocketState.CONNECTING || state == WebSocketState.OPEN) {
            websocket?.disconnect()
        }

        websocket = WebSocketFactory()
                .setVerifyHostname(false)
                .createSocket("$endpoint/?v=${Kordis.API_VERSION}&encoding=json", 10000)

        websocket!!.addListener(this)

        while (true) {
            try {
                websocket!!.connect()
                break
            } catch (ex: Exception) {
                LOGGER.warn("Couldn't connect to the the gateway. Retrying in 3 seconds.", ex)
                websocket!!.disconnect()
                websocket = websocket!!.recreate()
                delay(3000)
            }
        }

        while (!ready) {
            delay(100)
        }
    }

    internal fun requestMembers(serverId: Long) {
        memberRequestChannel.offer(serverId)
    }

    internal fun updateStatus(status: UserStatus, type: ActivityType, name: String) = start {
        activity = json {
            "since".toNull()
            "status" to status.id
            "afk" to false
            "game" to json {
                "name" to name
                "type" to type.id
            }
        }

        activity?.let { queue(Opcode.STATUS_UPDATE, it) }
    }

    override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) = start {
        LOGGER.info("Connected to the gateway")
        client.status = ConnectionStatus.CONNECTED

        if (sessionId == null) {
            LOGGER.info("Authenticating...")
            authenticate()
        } else {
            LOGGER.info("Resuming the session...")
            resume()
        }
    }

    override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) = start {
        val code = serverCloseFrame?.closeCode ?: clientCloseFrame?.closeCode ?: -1
        val reason = serverCloseFrame?.closeReason ?: clientCloseFrame?.closeReason

        LOGGER.info("WebSocket closed with code: $code, reason: '$reason', remote: $closedByServer")

        ready = false
        heartbeatTask?.cancel()

        if (code == 4014) {
            LOGGER.error("Invalid privilege intent(s) are specified. you must first go to your application in the Developer Portal and enable the toggle for the Privileged Intents you wish to use.")
            return@start
        }

        // Invalidate cache
        if (code == 4007 || code == 4990 || code == 4003) {
            sessionId = null
            lastSequence = null

            memberRequestChannel.clear()
            client.users.clear()
            client.privateChannels.clear()
        }

        delay(1000)
        connect()
    }

    override fun onTextMessage(websocket: WebSocket, text: String) = start {
        val payloads = gson.fromJson(text, JsonObject::class.java)
        val opcode = Opcode.values().find { it.code == payloads["op"].asInt }
        val data = payloads.getObjectOrNull("d")
        LOGGER.trace("Receive: $payloads")

        when (opcode) {
            Opcode.HELLO -> {
                LOGGER.debug("Starting heartbeat task")

                val period = data!!["heartbeat_interval"].asLong
                heartbeatAckReceived = true

                heartbeatTask?.cancel()
                heartbeatTask = timer(period) {
                    if (heartbeatAckReceived) {
                        heartbeatAckReceived = false
                        send(Opcode.HEARTBEAT, json { lastSequence?.let { "d" to it } })
                    } else {
                        websocket.sendClose(4000, "Heartbeat ACK wasn't received")
                    }
                }
            }
            Opcode.RECONNECT -> {
                LOGGER.info("Received reconnect request")
                websocket.sendClose(4001, "Received Reconnect Request")
            }
            Opcode.INVALID_SESSION -> {
                LOGGER.info("The session id is invalid")
                websocket.sendClose(4990, "Invalid Session")
            }
            Opcode.HEARTBEAT_ACK -> {
                LOGGER.debug("Received heartbeat ACK")
                heartbeatAckReceived = true
            }
            Opcode.DISPATCH -> {
                val eventType = payloads["t"].asString
                lastSequence = payloads["s"].asString.toInt()

                if (eventType == "READY" || eventType == "RESUMED") {
                    ready = true

                    data?.getOrNull("session_id")?.let {
                        sessionId = it.asString
                    }
                }

                handleEvent(eventType, data!!)
            }
            else -> {
                LOGGER.warn("Received a packet with unknown opcode: $data")
            }
        }
    }

    fun handleEvent(eventType: String, data: JsonObject) {
        try {
            handlers.find { it.eventType == eventType }?.handle(client, data)
        } catch (ex: Exception) {
            LOGGER.error("Failed to handle the event! (type: $eventType, json: $data)", ex)
        }
    }

    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        LOGGER.error("WebSocket Error", cause)
    }

    private fun authenticate() {
        // https://discordapp.com/developers/docs/topics/gateway#identify-example-identify

        send(Opcode.IDENTIFY, json {
            "token" to client.token

            if (intents.isNotEmpty()) {
                var value = 0
                intents.forEach {
                    value = value or it.flag
                }
                LOGGER.trace("Intent flag: $value")
                "intents" to value
            }

            "properties" to json {
                "\$os" to "who knows"
                "\$browser" to "who knows"
                "\$device" to "who knows"
            }

            "shard" to jsonArray {
                +JsonPrimitive(client.shard)
                +JsonPrimitive(client.maxShards)
            }

            activity?.let {
                "presence" to it
            }
        })
    }

    private suspend fun queue(opcode: Opcode, data: JsonObject = JsonObject()) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        sendChannel.send(json)
    }

    private fun resume() {
        val sessionId = sessionId ?: throw IllegalArgumentException("sessionId is null")
        send(Opcode.RESUME, json {
            "session_id" to sessionId
            "token" to client.token
            "seq" to lastSequence
        })
    }

    private fun send(opcode: Opcode, data: JsonObject = JsonObject()) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        websocket!!.sendText(json)
        rateLimiter.increment()
    }
}