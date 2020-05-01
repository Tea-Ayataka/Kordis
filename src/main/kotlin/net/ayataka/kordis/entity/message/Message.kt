package net.ayataka.kordis.entity.message

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.attachment.Attachment
import net.ayataka.kordis.entity.message.embed.Embed
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.entity.server.emoji.PartialEmoji
import net.ayataka.kordis.entity.server.emoji.PartialEmojiImpl
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.user.User
import java.time.Instant

interface Message : Entity {
    /**
     * The type of the message
     */
    val type: MessageType

    /**
     * The author of the message
     * This will return null if the message was sent by a webhook
     */
    val author: User?

    /**
     * Member instance of the author
     * This will return null if the message was not sent in a server
     */
    val member: Member?

    /**
     * The server the message was sent in
     * This will return null if the message was not sent in a server
     */
    val server: Server?

    /**
     * The text channel the message was sent in
     */
    val channel: TextChannel

    /**
     * The server text channel the message was sent in
     */
    val serverChannel: ServerTextChannel?
        get() = channel as? ServerTextChannel

    /**
     * The private text channel the message was sent in
     */
    val privateChannel: PrivateTextChannel?
        get() = channel as? PrivateTextChannel

    /**
     * The text content of the message
     */
    val content: String

    /**
     * The embed contents of the message
     */
    val embeds: Collection<Embed>

    /**
     * The attachments of the message
     */
    val attachments: Collection<Attachment>

    /**
     * The edited timestamp of the message
     */
    val editedTimestamp: Instant?

    /**
     * Whether the message contains '@everyone'
     */
    val mentionsEveryone: Boolean

    /**
     * Whether the message is pinned
     */
    val pinned: Boolean

    /**
     * Whether the message is a Text-to-Speech message
     */
    val tts: Boolean

    /**
     * Edit the message
     */
    suspend fun edit(text: String = "", embed: (EmbedBuilder.() -> Unit)? = null): Message

    /**
     * Delete the message
     */
    suspend fun delete() = channel.deleteMessage(id)

    /**
     * Get a list of users that reacted with the emoji
     */
    suspend fun getReactors(emoji: PartialEmoji): List<User>
    suspend fun getReactors(name: String, id: Long? = null) = getReactors(PartialEmojiImpl(id, name))

    /**
     * Add a reaction to the message
     */
    suspend fun addReaction(emoji: PartialEmoji)

    /**
     * Add a reaction to the message
     */
    suspend fun addReaction(name: String, id: Long? = null) = addReaction(PartialEmojiImpl(id, name))

    /**
     * Remove a reaction by the bot from the message
     */
    suspend fun removeReaction(emoji: PartialEmoji)
    suspend fun removeReaction(name: String, id: Long? = null) = removeReaction(PartialEmojiImpl(id, name))

    /**
     * Remove a reaction by the user from the message
     */
    suspend fun removeReaction(emoji: PartialEmoji, user: User)
    suspend fun removeReaction(name: String, id: Long? = null, user: User) = removeReaction(PartialEmojiImpl(id, name), user)

    /**
     * Remove all the reactions from the message
     */
    suspend fun clearReactions()
}