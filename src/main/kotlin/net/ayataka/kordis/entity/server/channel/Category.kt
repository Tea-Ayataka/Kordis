package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.UserPermissionOverwrite

interface Category : ServerChannel {
    override val name: String
    override val server: Server
    override val position: Int

    override val userPermissionOverwrites: Collection<UserPermissionOverwrite>
    override val rolePermissionOverwrites: Collection<RolePermissionOverwrite>
}