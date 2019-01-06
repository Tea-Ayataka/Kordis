package net.ayataka.kordis.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage
import java.util.concurrent.LinkedBlockingQueue

@Suppress("EXPERIMENTAL_API_USAGE")
class GatewayClient(
        private val client: DiscordClientImpl,
        private val endpoint: String
) : CoroutineScope, WebSocket.Listener {
    override val coroutineContext = newSingleThreadContext("Gateway Packet Handler")

    private val mutex = Mutex()
    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var sessionId: String? = null
    @Volatile private var lastSequence: Int? = null
    @Volatile private var heartbeatAckReceived = false
    @Volatile private var heartbeatTask: Job? = null
    @Volatile private var activity: JsonObject? = null

    private val gson = Gson()
    private val buffer = StringBuffer()
    private val sendQueue = LinkedBlockingQueue<String>()
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
                if (webSocket == null || webSocket?.isOutputClosed == true) {
                    continue
                }

                while (!rateLimiter.isLimited()) {
                    val json = sendQueue.take()
                    try {
                        webSocket?.sendText(json, true)?.get()
                        webSocket?.request(1)
                    } catch (ex: Exception) {
                        LOGGER.error("WebSocket error", ex)
                        continue
                    }
                    rateLimiter.increment()
                    LOGGER.trace("Sent: $json")
                }
            }
        }, "Gateway Packet Dispatcher").start()
    }

    suspend fun connect() = mutex.withLock {
        if (webSocket != null) {
            if (webSocket?.isOutputClosed == false) {
                webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "")?.await()
            }
            webSocket = null
        }

        webSocket = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI("$endpoint/?v=${Kordis.API_VERSION}&encoding=json"), this)
                .await()
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

    override fun onOpen(webSocket: WebSocket) {
        LOGGER.info("Connected to the gateway")
        client.status = ConnectionStatus.CONNECTED

        if (sessionId == null) {
            authenticate()
        } else {
            resume()
        }
    }

    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
        LOGGER.info("WebSocket closed with code: $statusCode, reason: '$reason'")

        heartbeatTask?.cancel()

        launch {
            // Invalidate cache
            if (statusCode == 4007 || statusCode == 4990) {
                sessionId = null
                lastSequence = null

                memberChunkRequestQueue.clear()
                client.users.clear()
                client.privateChannels.clear()
            }

            delay(1000)
            connect()
        }

        return null
    }

    override fun onText(webSocket: WebSocket, raw: CharSequence, last: Boolean): CompletionStage<*>? {
        launch {
            buffer.append(raw)
            webSocket.request(1)

            if (!last) {
                return@launch
            }

            val message = buffer.toString()
            buffer.setLength(0)

            val payloads = gson.fromJson(message, JsonObject::class.java)
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
                            queue(Opcode.HEARTBEAT, json { lastSequence?.let { "d" to it } })
                        } else {
                            webSocket.sendClose(4000, "Heartbeat ACK wasn't received")
                        }
                    }
                }
                Opcode.RECONNECT -> {
                    LOGGER.info("Received reconnect request")
                    webSocket.sendClose(4001, "Received Reconnect Request")
                }
                Opcode.INVALID_SESSION -> {
                    LOGGER.info("The session id is invalid")
                    webSocket.sendClose(4990, "Invalid Session")
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

        return null
    }

    fun handleEvent(eventType: String, data: JsonObject) {
        try {
            handlers.find { it.eventType == eventType }?.handle(client, data)
        } catch (ex: Exception) {
            LOGGER.error("Failed to handle the event! (type: $eventType, json: $data)")
            LOGGER.debug("packet handle error", ex)
        }
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        LOGGER.error("WebSocket exception", error)
    }

    private fun authenticate() {
        // https://discordapp.com/developers/docs/topics/gateway#identify-example-identify

        queue(Opcode.IDENTIFY, json {
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
        queue(Opcode.RESUME, json {
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
}