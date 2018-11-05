package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class PresenseUpdateHandler : GatewayHandler {
    override val eventName = "PRESENCE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}