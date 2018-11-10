package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.overwrite.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.overwrite.UserPermissionOverwrite

interface ServerChannel : Nameable, Entity {
    val server: Server
    val position: Int

    val userPermissionOverwrites: Collection<UserPermissionOverwrite>
    val rolePermissionOverwrites: Collection<RolePermissionOverwrite>

    /**
     * Delete the channel
     */
    suspend fun delete()
}