package net.ayataka.kordis.entity.server.channel.updater

import net.ayataka.kordis.entity.server.channel.ServerTextChannel

class ServerTextChannelUpdater(val channel: ServerTextChannel) : ServerChannelUpdater(channel) {
    var topic = channel.topic
    var categoy = channel.category
    var nsfw = channel.nsfw
    var rateLimitPerUser = channel.rateLimitPerUser
}