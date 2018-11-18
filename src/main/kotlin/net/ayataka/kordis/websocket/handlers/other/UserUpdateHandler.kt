package net.ayataka.kordis.websocket.handlers.other

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class UserUpdateHandler : GatewayHandler {
    override val eventType = "USER_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}