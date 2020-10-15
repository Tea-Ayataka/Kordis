@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.ayataka.kordis.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.neovisionaries.ws.client.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.GatewayIntent
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.server.member.MemberImpl
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.Inflater
import java.util.zip.InflaterOutputStream
import kotlin.collections.HashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val ZLIB_SUFFIX = hex("0000ffff")

class GatewayClient(
        private val client: DiscordClientImpl,
        private val endpoint: String,
        private val intents: Set<GatewayIntent>
) : WebSocketAdapter() {
    @Volatile
    private var ready: Boolean = false

    @Volatile
    private var websocket: WebSocket? = null

    @Volatile
    private var sessionId: String? = null

    @Volatile
    private var lastSequence: Int? = null

    @Volatile
    private var heartbeatAckReceived: Boolean = false

    @Volatile
    private var heartbeatTask: Job? = null

    @Volatile
    private var activity: JsonObject? = null

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("Gateway Client"))
    private val buffer = mutableListOf<ByteArray>()
    private val inflater = Inflater()
    private val mutex = Mutex()
    private val gson = Gson()
    private val sendChannel = Channel<String>(Channel.UNLIMITED)
    private val rateLimiter = RateLimiter(60 * 1000, 100) // The actual limit is 120
    private val nonceSeed = AtomicLong()

    data class MemberChunkRequest(val nonce: String, val handler: Continuation<List<MemberImpl>>)
    private val memberChunkRequests = LinkedList<MemberChunkRequest>()

    data class PostponedServerEvent(val eventType: String, val data: JsonObject)
    private val postponedServerEvents = HashMap<Long, ArrayDeque<PostponedServerEvent>>()

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
            PresenceUpdateHandler(),
            ReadyHandler(),
            TypingStartHandler(),
            UserUpdateHandler(),
            VoiceServerUpdateHandler(),
            VoiceStateUpdateHandler()
    )

    init {
        scope.launch(newSingleThreadContext("Gateway Packet Dispatcher")) {
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
    }

    suspend fun connect(): Unit = mutex.withLock {
        val state = websocket?.state
        if (state == WebSocketState.CONNECTING || state == WebSocketState.OPEN) {
            websocket?.disconnect()
        }

        websocket = WebSocketFactory()
                .setVerifyHostname(false)
                .createSocket("$endpoint/?v=${Kordis.API_VERSION}&encoding=json&compress=zlib-stream", 10000)

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

    internal fun updateStatus(status: UserStatus, type: ActivityType, name: String) {
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

    internal fun onReceivedMemberChunk(nonce: String, members: List<MemberImpl>) {
        synchronized(memberChunkRequests) {
            memberChunkRequests.firstOrNull { it.nonce == nonce }?.also {
                memberChunkRequests.remove(it)
            }
        }.also {
            it?.handler?.resume(members)
        }
    }

    internal suspend fun getMembers(serverId: Long, userIds: Array<Long>?, query: String?): List<MemberImpl> {
        if (userIds != null && userIds.size > 100) {
            throw IllegalArgumentException("the amount of 'userIds' cannot be greater than 100")
        }

        return suspendCoroutine {
            val nonce = "${serverId}-${nonceSeed.incrementAndGet()}"
            synchronized(memberChunkRequests) {
                memberChunkRequests.add(MemberChunkRequest(nonce, it))
            }

            queue(Opcode.REQUEST_GUILD_MEMBERS, json {
                "guild_id" to serverId
                "limit" to 0
                "nonce" to nonce

                if (userIds != null) {
                    "user_ids" to jsonArray { userIds.forEach { +JsonPrimitive(it) } }
                }

                if (query != null) {
                    "query" to query
                }
            })
        }
    }

    internal fun postponeServerEvent(eventType: String, data: JsonObject, _serverId: Long? = null) {
        if (eventType == GuildCreateHandler().eventType) {
            error("Cannot postpone GUILD_CREATE event")
        }

        synchronized(postponedServerEvents) {
            val serverId = _serverId ?: data["guild_id"].asLong
            if (client.servers.find(serverId) != null) {
                handleEvent(eventType, data)
                return
            }

            postponedServerEvents
                    .getOrPut(serverId) { ArrayDeque() }
                    .offer(PostponedServerEvent(eventType, data))
        }
    }

    internal fun handlePostponedServerEvents(server: ServerImpl) {
        synchronized(postponedServerEvents) {
            postponedServerEvents[server.id]?.also {
                while (it.isNotEmpty()) {
                    val event = it.poll()
                    handleEvent(event.eventType, event.data)
                }

                postponedServerEvents.remove(server.id)
            }
        }
    }

    override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
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

    override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
        val code = serverCloseFrame?.closeCode ?: clientCloseFrame?.closeCode ?: -1
        val reason = serverCloseFrame?.closeReason ?: clientCloseFrame?.closeReason

        LOGGER.info("WebSocket closed with code: $code, reason: '$reason', remote: $closedByServer")

        ready = false
        heartbeatTask?.cancel()
        buffer.clear()
        inflater.reset()

        if (code == 4014) {
            LOGGER.error("Invalid privilege intent(s) are specified. you must first go to your application in the Developer Portal and enable the toggle for the Privileged Intents you wish to use.")
            return
        }

        // Invalidate cache
        if (code == 4007 || code == 4990 || code == 4003) {
            sessionId = null
            lastSequence = null

            postponedServerEvents.clear()
            client.users.clear()
            client.privateChannels.clear()
        }

        scope.launch {
            connect()
        }
    }

    override fun onBinaryMessage(websocket: WebSocket, binary: ByteArray) {
        // Add the received data to buffer
        buffer.add(binary)

        // Check for zlib suffix
        if (binary.size < 4 || !binary.takeLastAsByteArray(4).contentEquals(ZLIB_SUFFIX)) {
            return
        }

        // Decompress the buffered data
        val text = ByteArrayOutputStream().use { output ->
            try {
                InflaterOutputStream(output, inflater).use {
                    it.write(buffer.concat())
                }
                output.toString(Charsets.UTF_8.toString())
            } catch (e: IOException) {
                LOGGER.error("Error while decompressing payload", e)
                return
            } finally {
                buffer.clear()
            }
        }

        // Handle the message
        handleMessage(websocket, text)
    }

    private fun handleMessage(websocket: WebSocket, text: String) {
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
                heartbeatTask = scope.timer(period) {
                    if (heartbeatAckReceived) {
                        heartbeatAckReceived = false
                        send(Opcode.HEARTBEAT, json { lastSequence?.let { "d" to it } })
                    } else {
                        websocket.sendClose(4000, "Heartbeat ACK wasn't received")
                    }
                }
            }
            Opcode.RECONNECT -> {
                LOGGER.info("Received RECONNECT opcode. Attempting to reconnect.")
                websocket.sendClose(4001, "Received Reconnect Request")
            }
            Opcode.INVALID_SESSION -> {
                LOGGER.info("Received INVALID_SESSION opcode. Attempting to reconnect.")
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

                    LOGGER.info("Ready")
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

    private fun queue(opcode: Opcode, data: JsonObject = JsonObject()) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        sendChannel.offer(json)
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

        LOGGER.trace("Sent: $json")
        websocket!!.sendText(json)
        rateLimiter.increment()
    }
}