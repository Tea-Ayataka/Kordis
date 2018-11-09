package net.ayataka.kordis.entity.server.channel.updater

import net.ayataka.kordis.entity.server.channel.ServerChannel

open class ServerChannelUpdater(channel: ServerChannel) {
    val name = channel.name
    val position = channel.position
    val userPermissionOverwrites = channel.userPermissionOverwrites.toMutableSet()
    val rolePermissionOverwrites = channel.rolePermissionOverwrites.toMutableSet()
}