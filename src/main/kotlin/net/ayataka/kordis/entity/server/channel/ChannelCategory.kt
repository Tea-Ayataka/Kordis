package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.server.channel.updater.ServerChannelUpdater

interface ChannelCategory : ServerChannel {
    /**
     * Edit the channel category
     */
    suspend fun edit(block: ServerChannelUpdater.() -> Unit)
}