package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageDeleteBulkHandler : GatewayHandler {
    override val eventName = "MESSAGE_DELETE_BULK"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}