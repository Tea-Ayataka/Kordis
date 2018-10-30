package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class TypingStartHandler : GatewayHandler {
    override val eventName = "TYPING_START"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}