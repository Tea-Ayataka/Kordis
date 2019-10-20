package net.ayataka.kordis.entity.server.channel.store

import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory

interface StoreChannel : ServerChannel, Mentionable {
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
}