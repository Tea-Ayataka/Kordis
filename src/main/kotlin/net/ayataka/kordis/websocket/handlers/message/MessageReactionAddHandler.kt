package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageReactionAddHandler : GatewayHandler {
    override val eventName = "MESSAGE_REACTION_ADD"
    override fun handle(client: DiscordClient, data: JsonObject) {

    }

}