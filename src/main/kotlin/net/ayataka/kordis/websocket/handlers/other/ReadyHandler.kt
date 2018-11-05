package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ReadyHandler : GatewayHandler {
    override val eventName = "READY"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}