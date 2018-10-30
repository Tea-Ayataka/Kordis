package net.ayataka.kordis.websocket

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.LOGGER
import net.ayataka.kordis.utils.start
import net.ayataka.kordis.utils.timer
import net.ayataka.kordis.websocket.handlers.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

const val GATEWAY_VERSION = 6

class GatewayClient(
        private val client: DiscordClient,
        endpoint: String
) : CoroutineScope, WebSocketClient(URI("$endpoint/?v=$GATEWAY_VERSION&encoding=json")) {


    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + job + CoroutineName("WebSocket Handler")

    @Volatile
    private var lastSequence = -1
    @Volatile
    private var isHeartbeatAckReceived = false
    private var heartbeatTask: Job? = null

    private val handlers = mapOf(
            "CHANNEL_CREATE" to ChannelCreateHandler(),
            "CHANNEL_DELETE" to ChannelDeleteHandler(),
            "CHANNEL_UPDATE" to ChannelUpdateHandler(),
            "GUILD_BAN_ADD" to GuildBanAddHandler(),
            "GUILD_BAN_REMOVE" to GuildBanRemoveHandler(),
            "GUILD_CREATE" to GuildCreateHandler(),
            "GUILD_DELETE" to GuildDeleteHandler(),
            "GUILD_EMOJIS_UPDATE" to GuildEmojisUpdateHandler(),
            "GUILD_MEMBER_ADD" to GuildMemberAddHandler(),
            "GUILD_MEMBER_REMOVE" to GuildMemberRemoveHandler(),
            "GUILD_MEMBER_UPDATE" to GuildMemberUpdateHandler(),
            "GUILD_MEMBERS_CHUNK" to GuildMembersChunkHandler(),
            "GUILD_ROLE_CREATE" to GuildRoleCreateHandler(),
            "GUILD_ROLE_DELETE" to GuildRoleDeleteHandler(),
            "GUILD_ROLE_UPDATE" to GuildRoleUpdateHandler(),
            "GUILD_SYNC" to GuildSyncHandler(),
            "GUILD_UPDATE" to GuildUpdateHandler(),
            "MESSAGE_CREATE" to MessageCreateHandler(),
            "MESSAGE_DELETE" to MessageDeleteHandler(),
            "MESSAGE_DELETE_BULK" to MessageDeleteBulkHandler(),
            "MESSAGE_REACTION_ADD" to MessageReactionAddHandler(),
            "MESSAGE_REACTION_REMOVE" to MessageReactionRemoveHandler(),
            "MESSAGE_REACTION_REMOVE_ALL" to MessageReactionRemoveAllHandler(),
            "MESSAGE_UPDATE" to MessageUpdateHandler(),
            "PRESENCE_UPDATE" to PresenseUpdateHandler(),
            "READY" to ReadyHandler(),
            "TYPING_START" to TypingStartHandler(),
            "USER_UPDATE" to UserUpdateHandler(),
            "VOICE_SERVER_UPDATE" to VoiceServerUpdateHandler(),
            "VOICE_STATE_UPDATE" to VoiceStateUpdateHandler()
    )

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
                        send(Opcode.HEARTBEAT)
                        LOGGER.info("Sent heartbeat (${period}ms)")
                    } else {
                        LOGGER.info("Heartbeat ACK wasn't received. Reconnecting...")
                        reconnect()
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
                handlers[eventName]!!.handle(client, data!!)
            }
            else -> {
                LOGGER.warn("Received an unknown packet (opcode: $opcode, data: $data)")
            }
        }
    }

    override fun onError(ex: Exception) {
        LOGGER.error("WebSocket exception", ex)
    }

    private suspend fun authenticate() {
        // https://discordapp.com/developers/docs/topics/gateway#identify-example-identify

        send(Opcode.IDENTIFY, json {
            "token" to client.token

            "properties" to json {
                "\$os" to "who knows"
                "\$browser" to "who knows"
                "\$device" to "who knows"
            }
        })
    }

    private suspend fun send(opcode: Opcode, data: JsonObject = JsonObject(mapOf())) {
        val json = json {
            "op" to opcode.code
            "d" to data
        }.toString()

        launch { send(json) }.join()
        LOGGER.debug("Sent: $json")
    }
}