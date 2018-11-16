package net.ayataka.kordis.entity.server.role

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.Permissionable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.PermissionSet
import java.awt.Color

interface Role : Mentionable, Nameable, Permissionable, Entity {
    /**
     * The server of the role
     */
    val server: Server

    /**
     * The allowed permissions of the role
     */
    val permissions: PermissionSet

    /**
     * The color of the role
     */
    val color: Color

    /**
     * The position of the role
     */
    val position: Int

    /**
     * Whether the role is hoisted
     *
     * Members in a hoisted role are displayed in their own grouping on the member list
     */
    val hoist: Boolean

    /**
     * Whether the role is managed
     */
    val managed: Boolean

    /**
     * Whether the role is mentionable
     */
    val mentionable: Boolean

    /**
     * Whether ths role is the '@everyone' role
     */
    val isEveryone: Boolean
        get() = position == 0 && name == "@everyone"

    /**
     * The mention tag of the role
     */
    override val mention: String
        get() = if (position == 0) "@everyone" else "<@&$id>"

    /**
     * Edit the role
     */
    suspend fun edit(block: RoleBuilder.() -> Unit)

    /**
     * Delete the role
     */
    suspend fun delete()
}