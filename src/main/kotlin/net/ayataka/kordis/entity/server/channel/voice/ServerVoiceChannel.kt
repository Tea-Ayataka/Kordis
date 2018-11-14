package net.ayataka.kordis.entity.server.channel.voice

import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.ServerChannel

interface ServerVoiceChannel : ServerChannel {
    /**
     * The category of the channel
     */
    val category: ChannelCategory?

    /**
     * The bitrate of the voice channel
     */
    val bitrate: Int

    /**
     * The user limit of the voice channel
     * 0 for unlimited
     */
    val userLimit: Int

    /**
     * Edit the voice channel
     */
    suspend fun edit(block: ServerVoiceChannelBuilder.() -> Unit)
}