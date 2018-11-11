package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageReactionAddHandler : GatewayHandler {
    override val eventType = "MESSAGE_REACTION_ADD"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }

}