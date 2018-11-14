package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageReactionRemoveAllHandler : GatewayHandler {
    override val eventType = "MESSAGE_REACTION_REMOVE_ALL"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}