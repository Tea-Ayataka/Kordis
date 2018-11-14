package net.ayataka.kordis.entity.server.channel.text

import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder

class ServerTextChannelBuilder(channel: ServerTextChannel? = null) : ServerChannelBuilder(channel) {
    var topic = channel?.topic
    var category = channel?.category
    var nsfw = channel?.nsfw ?: false
    var rateLimitPerUser = channel?.rateLimitPerUser ?: 0
}