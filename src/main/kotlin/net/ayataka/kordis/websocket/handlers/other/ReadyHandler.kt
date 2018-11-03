package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ReadyHandler : GatewayHandler {
    override val eventName = "READY"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}