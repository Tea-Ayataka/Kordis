package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.updater.ServerTextChannelUpdater

interface ServerTextChannel : ServerChannel, Mentionable, TextChannel {
    /**
     * The topic of the channel
     */
    val topic: String

    /**
     * Whether the channel is a nsfw channel or not
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
    suspend fun edit(block: ServerTextChannelUpdater.() -> Unit)
}