package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageDeleteHandler: GatewayHandler {
    override val eventType = "MESSAGE_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}