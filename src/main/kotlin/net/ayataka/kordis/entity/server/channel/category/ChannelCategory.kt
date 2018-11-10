package net.ayataka.kordis.entity.server.channel.category

import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder

interface ChannelCategory : ServerChannel {
    /**
     * Edit the channel category
     */
    suspend fun edit(block: ServerChannelBuilder.() -> Unit)
}