package net.ayataka.kordis.websocket

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.LOGGER
import net.ayataka.kordis.utils.*
import net.ayataka.kordis.websocket.handlers.channel.ChannelCreateHandler
import net.ayataka.kordis.websocket.handlers.channel.ChannelDeleteHandler
import net.ayataka.kordis.websocket.handlers.channel.ChannelUpdateHandler
import net.ayataka.kordis.websocket.handlers.guild.*
import net.ayataka.kordis.websocket.handlers.message.*
import net.ayataka.kordis.websocket.handlers.other.PresenseUpdateHandler
import net.ayataka.kordis.websocket.handlers.other.ReadyHandler
import net.ayataka.kordis.websocket.handlers.other.TypingStartHandler
import net.ayataka.kordis.websocket.handlers.other.UserUpdateHandler
import net.ayataka.kordis.websocket.handlers.voice.VoiceServerUpdateHandler
import net.ayataka.kordis.websocket.handlers.voice.VoiceStateUpdateHandler
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.ConcurrentLinkedQueue

const val GATEWAY_VERSION = 6

class GatewayClient(
        private val client: DiscordClientImpl,
        endpoint: String
) : CoroutineScope, WebSocketClient(URI("$endpoint/?v=$GATEWAY_VERSION&encoding=json")) {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + job + CoroutineName("WebSocket Handler")

    @Volatile
    private var lastSequence = -1
    @Volatile
    private var isHeartbeatAckReceived = false
    private var heartbeatTask: Job? = null
    private val sendQueue = ConcurrentLinkedQueue<String>()
    private val rateLimiter = RateLimiter(60.seconds(), 100) // The actual limit is 120

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
            GuildSyncHandler(),
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
        timer(30, false, CoroutineName("WebSocket Packet Dispatcher")) {
            if (isClosing || isClosed) {
                return@timer
            }

            while (!rateLimiter.isLimited()) {
                val json = sendQueue.poll() ?: return@timer
                send(json)
                rateLimiter.increment()
                LOGGER.debug("Sent: $json")
            }
        }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        LOGGER.info("Connected to the gateway with code ${handshakedata.httpStatus} (${handshakedata.httpStatusMessage})")
        client.status = ConnectionStatus.CONNECTED
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        LOGGER.warn("Websocket closed with code $code reason $reason. Reconnecting in a second...")

        heartbeatTask?.cancel()

        launch {
            delay(1000)
            reconnect()
        }
    }

    override fun onMessage(message: String) = start {
        val payloads = JsonTreeParser(message).readFully().jsonObject
        val opcode = Opcode.values().find { it.code == payloads["op"].int }
        val data = if (!payloads["d"].isNull) payloads["d"].jsonObject else null

        LOGGER.debug("Gateway payload received ($opcode) - $payloads")

        when (opcode) {
            Opcode.HELLO -> {
                LOGGER.info("Starting heartbeat task")

                val period = data!!["heartbeat_interval"].long

                isHeartbeatAckReceived = true
                heartbeatTask = timer(period) {
                    if (isHeartbeatAckReceived) {
                        isHeartbeatAckReceived = false
                        queue(Opcode.HEARTBEAT)
                        LOGGER.info("Sent heartbeat (${period}ms)")
                    } else {
                        LOGGER.info("Heartbeat ACK wasn't received. Reconnecting...")
                        close()
                    }
                }

                LOGGER.info("Authenticating")
                authenticate()
            }
            Opcode.HEARTBEAT_ACK -> {
                LOGGER.info("Received heartbeat ACK")
                isHeartbeatAckReceived = true
            }
            Opcode.DISPATCH -> {
                val eventName = payloads["t"].content
                LOGGER.info("Received an event ($eventName) $data")

                // Handle the event
                handlers.find { it.eventName == eventName }!!.handle(client, data!!)
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