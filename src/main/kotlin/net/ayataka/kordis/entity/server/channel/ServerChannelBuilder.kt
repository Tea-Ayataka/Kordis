package net.ayataka.kordis.entity.server.channel

open class ServerChannelBuilder(channel: ServerChannel? = null) {
    var name = channel?.name
    var position = channel?.position
    val userPermissionOverwrites = channel?.userPermissionOverwrites?.toMutableSet() ?: mutableSetOf()
    val rolePermissionOverwrites = channel?.rolePermissionOverwrites?.toMutableSet() ?: mutableSetOf()
}