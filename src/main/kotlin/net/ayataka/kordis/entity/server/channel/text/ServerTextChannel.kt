package net.ayataka.kordis.entity.server.channel.text

import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory

interface ServerTextChannel : ServerChannel, Mentionable, TextChannel {
    /**
     * The topic of the channel
     */
    val topic: String

    /**
     * Whether the channel is a nsfw channel
     */
    val nsfw: Boolean

    /**
     * The ratelimit of the channel
     */
    val rateLimitPerUser: Int

    /**
     * The category of the channel
     */
    val category: ChannelCategory?

    /**
     * The mention tag of the channel
     */
    override val mention: String
        get() = "<#$id>"

    /**
     * Edit the channel
     */
    suspend fun edit(block: ServerTextChannelBuilder.() -> Unit)

    /**
     * Bulk delete messages
     */
    suspend fun deleteMessages(messageIds: Collection<Long>)
}