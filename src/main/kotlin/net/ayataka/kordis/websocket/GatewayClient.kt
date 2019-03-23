package net.ayataka.kordis.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClientImpl
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
import okhttp3.*
import java.util.concurrent.LinkedBlockingDeque

@Suppress("EXPERIMENTAL_API_USAGE")
class GatewayClient(
        private val client: DiscordClientImpl,
        private val endpoint: String
) : CoroutineScope, WebSocketListener() {
    override val coroutineContext = newSingleThreadContext("Gateway Packet Handler")

    @Volatile private var ready: Boolean = false
    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var sessionId: String? = null
    @Volatile private var lastSequence: Int? = null
    @Volatile private var heartbeatAckReceived: Boolean = false
    @Volatile private var heartbeatTask: Job? = null
    @Volatile private var activity: JsonObject? = null

    private val mutex = Mutex()
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private val sendQueue = LinkedBlockingDeque<String>()
    private val rateLimiter = RateLimiter(60 * 1000, 100) // The actual limit is 120

    val memberChunkRequestQueue = AdvancedQueue<Long>(50) {
        queue(Opcode.REQUEST_GUILD_MEMBERS, json {
            "guild_id" to jsonArray { it.forEach { +JsonPrimitive(it) } }
            "query" to ""
            "limit" to 0
        })

        delay(500)
    }

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
        Thread({
            while (true) {
                while (webSocket != null && ready && !rateLimiter.isLimited()) {
                    val json = sendQueue.take()
                    try {
                        if (webSocket?.send(json) != true) {
                            sendQueue.offerFirst(json)
                            break
                        }
                    } catch (ex: Exception) {
                        LOGGER.error("WebSocket error", ex)
                        sendQueue.offerFirst(json)
                        break
                    }
                    rateLimiter.increment()
                    LOGGER.trace("Sent: $json")
                }

                Thread.sleep(100)
            }
        }, "Gateway Packet Dispatcher").start()
    }

    suspend fun connect() = mutex.withLock {
        if (webSocket != null) {
            webSocket?.close(1000, null)
            webSocket = null
        }

        val request = Request.Builder().url("$endpoint/?v=${Kordis.API_VERSION}&encoding=json").build()
        webSocket = httpClient.newWebSocket(request, this)
    }

    fun updateStatus(status: UserStatus, type: ActivityType, name: String) {
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

    override fun onOpen(webSocket: WebSocket, response: Response) = start {
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

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) = start {
        ready = false
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = start {
        LOGGER.info("WebSocket closed with code: $code, reason: '$reason'")

        sendQueue.clear()
        heartbeatTask?.cancel()

        // Invalidate cache
        if (code == 4007 || code == 4990 || code == 4003) {
            sessionId = null
            lastSequence = null

            memberChunkRequestQueue.clear()
            client.users.clear()
            client.privateChannels.clear()
        }

        delay(1000)
        connect()
    }

    override fun onMessage(webSocket: WebSocket, text: String) = start {
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
                        webSocket.close(4000, "Heartbeat ACK wasn't received")
                    }
                }
            }
            Opcode.RECONNECT -> {
                LOGGER.info("Received reconnect request")
                webSocket.close(4001, "Received Reconnect Request")
            }
            Opcode.INVALID_SESSION -> {
                LOGGER.info("The session id is invalid")
                webSocket.close(4990, "Invalid Session")
            }
            Opcode.HEARTBEAT_ACK -> {
                LOGGER.debug("Received heartbeat ACK")
                heartbeatAckReceived = true
            }
            Opcode.DISPATCH -> {
                val eventType = payloads["t"].asString
                lastSequence = payloads["s"].asString.toInt()

                when (eventType) {
                    "READY" -> {
                        ready = true
                        sessionId = data!!["session_id"].asString
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

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = start {
        LOGGER.error("WebSocket exception", t)
        onClosed(webSocket, -1, "Error")
    }

    private fun authenticate() {
        // https://discordapp.com/developers/docs/topics/gateway#identify-example-identify

        send(Opcode.IDENTIFY, json {
            "token" to client.token

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

    private fun resume() {
        val sessionId = sessionId ?: throw IllegalArgumentException("sessionId is null")
        send(Opcode.RESUME, json {
            "session_id" to sessionId
            "token" to client.token
            "seq" to lastSequence
        })
    }

    private fun queue(opcode: Opcode, data: JsonObject = JsonObject()) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        sendQueue.offer(json)
    }

    private fun send(opcode: Opcode, data: JsonObject = JsonObject()) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        if (webSocket?.send(json) == true) {
            rateLimiter.increment()
        }
    }
}