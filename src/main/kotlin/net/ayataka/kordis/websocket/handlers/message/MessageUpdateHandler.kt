package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageUpdateHandler : GatewayHandler {
    override val eventType = "MESSAGE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        TODO("not implemented")
    }
}