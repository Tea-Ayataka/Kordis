package net.ayataka.kordis.entity.server.channel.updater

import net.ayataka.kordis.entity.server.channel.ServerVoiceChannel

class ServerVoiceChannelUpdater(val channel: ServerVoiceChannel) : ServerChannelUpdater(channel) {
    var bitrate = channel.bitrate
    var category = channel.category
    var userLimit = channel.userLimit
}