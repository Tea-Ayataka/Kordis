package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageDeleteBulkHandler : GatewayHandler {
    override val eventType = "MESSAGE_DELETE_BULK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}