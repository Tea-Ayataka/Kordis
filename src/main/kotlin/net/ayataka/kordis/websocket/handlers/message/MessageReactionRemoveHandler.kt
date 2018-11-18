package net.ayataka.kordis.websocket.handlers.message

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageReactionRemoveHandler  : GatewayHandler {
    override val eventType = "MESSAGE_REACTION_REMOVE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}