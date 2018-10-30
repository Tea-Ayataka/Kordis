package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageCreateHandler : GatewayHandler {
    override val eventName = "MESSAGE_CREATE"
    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}