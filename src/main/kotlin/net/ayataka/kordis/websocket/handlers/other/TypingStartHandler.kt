package net.ayataka.kordis.websocket.handlers.other

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class TypingStartHandler : GatewayHandler {
    override val eventType = "TYPING_START"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}