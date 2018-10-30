package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageUpdateHandler : GatewayHandler {
    override val eventName = "MESSAGE_UPDATE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}