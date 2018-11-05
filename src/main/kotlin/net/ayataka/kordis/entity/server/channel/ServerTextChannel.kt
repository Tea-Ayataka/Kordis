package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.UserPermissionOverwrite

interface ServerTextChannel : ServerChannel, TextChannel {
    override val name: String
    val server: Server
    val topic: String?
    val isNsfw: Boolean
    val rateLimitPerUser: Int
    val position: Int
    val category: Category?

    val userPermissionOverwrites: Collection<UserPermissionOverwrite>
    val rolePermissionsOverwrites: Collection<RolePermissionOverwrite>
}