package net.ayataka.kordis.entity.server.channel.voice

import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder

class ServerVoiceChannelBuilder(channel: ServerVoiceChannel? = null) : ServerChannelBuilder(channel) {
    var bitrate = channel?.bitrate
    var category = channel?.category
    var userLimit = channel?.userLimit ?: 0
}