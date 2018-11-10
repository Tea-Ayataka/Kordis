package net.ayataka.kordis.entity.message

import net.ayataka.kordis.entity.Entity
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
     * The type of this message
     */
    val type: MessageType

    /**
     * The author of this message
     * This will return null if this message was sent by a webhook
     */
    val author: User?

    /**
     * Member instance of the author
     * This will return null if this message was not sent in a server
     */
    val member: Member?
        get() = author?.let { server?.members?.find(it.id) }

    /**
     * The server this message was sent in
     * This will return null if this message was not sent in a server
     */
    val server: Server?

    /**
     * The text channel this message was sent in
     */
    val channel: TextChannel

    /**
     * The server text channel this message was sent in
     */
    val serverChannel: ServerTextChannel?
        get() = channel as? ServerTextChannel

    /**
     * The text content of this message
     */
    val content: String

    /**
     * The embed contents of this message
     */
    val embeds: Collection<Embed>

    /**
     * The edited timestamp of this message
     */
    val editedTimestamp: Instant?

    /**
     * Whether this message contains '@everyone' or not
     */
    val mentionsEveryone: Boolean

    /**
     * Whether this message is pinned or not
     */
    val pinned: Boolean

    /**
     * Whether this message is a Text-to-Speech message or not
     */
    val tts: Boolean

    /**
     * Edit this message
     */
    suspend fun edit(text: String = "", embed: (EmbedBuilder.() -> Unit)? = null): Message
}