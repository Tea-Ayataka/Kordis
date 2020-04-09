package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.message.reaction.Reaction
import net.ayataka.kordis.entity.message.reaction.ReactionImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.event.Event

/**
 * Common base for reaction events.
 */
interface ReactionEvent : Event {
    val server: Server?
    val reaction: Reaction
}

/**
 * Represents the [Message Reaction Add](https://discordapp.com/developers/docs/topics/gateway#message-reaction-add)
 * event.
 */
data class ReactionAddEvent(override val reaction: Reaction) : ReactionEvent {
    override val server = reaction.server
}
/**
 * Represents the [Message Reaction Remove](https://discordapp.com/developers/docs/topics/gateway#message-reaction-remove)
 * event.
 */
data class ReactionRemoveEvent(override val reaction: Reaction) : ReactionEvent {
    override val server = reaction.server
}
