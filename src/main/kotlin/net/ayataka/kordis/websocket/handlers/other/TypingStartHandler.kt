package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class TypingStartHandler : GatewayHandler {
    override val eventName = "TYPING_START"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}