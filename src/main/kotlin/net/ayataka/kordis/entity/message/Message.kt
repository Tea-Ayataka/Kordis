package net.ayataka.kordis.entity.message

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.embed.Embed
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
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
        get() = author?.let { server?.members?.find(it.id) }

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
     * Get the attachments of the message
     */
    // TODO: not implemented

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
}