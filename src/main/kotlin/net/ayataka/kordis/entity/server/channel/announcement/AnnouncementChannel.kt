package net.ayataka.kordis.entity.server.channel.announcement

import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory

interface AnnouncementChannel : ServerChannel, Mentionable, TextChannel {
    /**
     * The topic of the channel
     */
    val topic: String?

    /**
     * Whether the channel is a nsfw channel
     */
    val nsfw: Boolean

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
     * Bulk delete messages
     */
    suspend fun deleteMessages(messageIds: Collection<Long>)
}