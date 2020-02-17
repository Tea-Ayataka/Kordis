package net.ayataka.kordis.entity.message.reaction

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.emoji.PartialEmoji
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.user.User

/**
 * Represents a reaction.
 *
 * **See** [Message Reaction Add](https://discordapp.com/developers/docs/topics/gateway#message-reaction-add)
 */
interface Reaction {
    val server: Server?
    val userId: Long
    val channelId: Long
    val messageId: Long
    val author: User?
    val member: Member?
    val emoji: PartialEmoji
}
