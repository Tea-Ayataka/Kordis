package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class UserUpdateHandler : GatewayHandler {
    override val eventName = "USER_UPDATE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}