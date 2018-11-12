package net.ayataka.kordis.websocket

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.API_VERSION
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.utils.AdvancedQueue
import net.ayataka.kordis.utils.RateLimiter
import net.ayataka.kordis.utils.start
import net.ayataka.kordis.utils.timer
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
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.LinkedBlockingQueue

@Suppress("EXPERIMENTAL_API_USAGE")
class GatewayClient(
        private val client: DiscordClientImpl,
        endpoint: String
) : CoroutineScope, WebSocketClient(URI("$endpoint/?v=$API_VERSION&encoding=json")) {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + job + CoroutineName("WebSocket Handler")

    @Volatile private var sessionId: String? = null
    @Volatile private var lastSequence = -1
    @Volatile private var heartbeatAckReceived = false
    @Volatile private var heartbeatTask: Job? = null
    @Volatile private var activity: JsonObject? = null

    private val sendQueue = LinkedBlockingQueue<String>()
    private val rateLimiter = RateLimiter(60 * 1000, 100) // The actual limit is 120

    val memberChunkRequestQueue = AdvancedQueue<Long>(50) {
        queue(Opcode.REQUEST_GUILD_MEMBERS, json {
            "guild_id" to JsonArray(it.map { JsonPrimitive(it) })
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
                if (isClosing || isClosed) {
                    continue
                }

                while (!rateLimiter.isLimited()) {
                    val json = sendQueue.take()
                    send(json)
                    rateLimiter.increment()
                    LOGGER.debug("Sent: $json")
                }
            }
        }, "WebSocket Packet Dispatcher").start()
    }

    fun updateStatus(status: UserStatus, type: ActivityType, name: String) {
        activity = json {
            "since" to JsonNull
            "status" to status.id
            "afk" to false
            "game" to json {
                "name" to name
                "type" to type.id
            }
        }

        activity?.let { queue(Opcode.STATUS_UPDATE, it) }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        LOGGER.info("Connected to the gateway with code ${handshakedata.httpStatus} (${handshakedata.httpStatusMessage})")
        client.status = ConnectionStatus.CONNECTED

        if (sessionId == null) {
            authenticate()
        } else {
            resume()
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        LOGGER.warn("WebSocket closed with code: $code reason: $reason remote: $remote")

        heartbeatTask?.cancel()

        launch {
            // Invalidate cache
            if (code == 4007 || (code == 1000 && !remote)) {
                sessionId = null

                memberChunkRequestQueue.clear()
                client.users.clear()
                client.privateChannels.clear()
            }

            delay(1000)
            reconnect()
        }
    }

    override fun onMessage(message: String) = start {
        val payloads = JsonTreeParser(message).readFully().jsonObject
        val opcode = Opcode.values().find { it.code == payloads["op"].int }
        val data = payloads.getObjectOrNull("d")
        LOGGER.debug("Gateway payload received ($opcode) - $payloads")

        when (opcode) {
            Opcode.HELLO -> {
                LOGGER.debug("Starting heartbeat task")

                val period = data!!["heartbeat_interval"].long
                heartbeatAckReceived = true

                heartbeatTask = timer(period, context = newSingleThreadContext("Heartbeat Dispatcher")) {
                    if (heartbeatAckReceived) {
                        heartbeatAckReceived = false
                        queue(Opcode.HEARTBEAT, json { if (lastSequence > 0) "d" to lastSequence })
                    } else {
                        close(2000, "Heartbeat ACK wasn't received")
                    }
                }
            }
            Opcode.RECONNECT -> {
                LOGGER.info("Received reconnect request")
                close(2001, "Received Reconnect Request")
            }
            Opcode.INVALID_SESSION -> {
                LOGGER.info("The session id is invalid")
                close(1000, "Invalid Session")
            }
            Opcode.HEARTBEAT_ACK -> {
                LOGGER.debug("Received heartbeat ACK")
                heartbeatAckReceived = true
            }
            Opcode.DISPATCH -> {
                val eventType = payloads["t"].content
                lastSequence = payloads["s"].content.toInt()

                LOGGER.debug("Received an event ($eventType) $data")

                when (eventType) {
                    "READY" -> {
                        sessionId = data!!["session_id"].content
                    }
                }

                try {
                    handlers.find { it.eventType == eventType }?.handle(client, data!!)
                } catch (ex: Exception) {
                    LOGGER.error("Failed to handle the event! (type: $eventType, error: ${ex.message})")
                    LOGGER.debug("packet handle error", ex)
                }
            }
            else -> {
                LOGGER.warn("Received an unknown packet (opcode: $opcode, data: $data)")
            }
        }
    }

    override fun onError(ex: Exception) {
        LOGGER.error("WebSocket exception", ex)
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

    private fun queue(opcode: Opcode, data: JsonObject = JsonObject(mapOf())) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        sendQueue.offer(json)
    }
}