package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.UserPermissionOverwrite

interface ServerChannel : Nameable, Entity {
    override val name: String
    val server: Server
    val position: Int

    val userPermissionOverwrites: Collection<UserPermissionOverwrite>
    val rolePermissionOverwrites: Collection<RolePermissionOverwrite>
}