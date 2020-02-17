package net.ayataka.kordis.websocket.handlers.message

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.reaction.ReactionImpl
import net.ayataka.kordis.event.events.message.ReactionRemoveEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

/**
 * Constructs and fires MESSAGE_REACTION_REMOVE event.
 *
 * @see [ReactionRemoveEvent]
 */
class MessageReactionRemoveHandler : GatewayHandler {
    override val eventType = "MESSAGE_REACTION_REMOVE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        client.eventManager.fire(ReactionRemoveEvent(ReactionImpl(client, data)))
    }
}
