package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.overwrite.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.overwrite.UserPermissionOverwrite

interface ServerChannel : Nameable, Entity {
    /**
     * The parent server of the channel
     */
    val server: Server

    /**
     * The position of the channel
     */
    val position: Int

    /**
     * The permission overwrites for users
     */
    val userPermissionOverwrites: Collection<UserPermissionOverwrite>

    /**
     * The permission overwrites for roles
     */
    val rolePermissionOverwrites: Collection<RolePermissionOverwrite>

    /**
     * Delete the channel
     */
    suspend fun delete()
}