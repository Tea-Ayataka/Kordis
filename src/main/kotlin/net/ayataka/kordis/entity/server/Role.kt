package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.updater.RoleUpdater
import java.awt.Color

interface Role : Mentionable, Nameable, Entity {
    /**
     * The server of this role
     */
    val server: Server

    /**
     * The allowed permissions of this role
     */
    val permissions: PermissionSet

    /**
     * The color of this role
     */
    val color: Color

    /**
     * The position of this role
     */
    val position: Int

    /**
     * Whether this role is hoisted
     *
     * Members in a hoisted role are displayed in their own grouping on the member list
     */
    val hoist: Boolean

    /**
     * Whether this role is managed
     */
    val managed: Boolean

    /**
     * Whether this role is mentionable
     */
    val mentionable: Boolean

    /**
     * The mention tag of this role
     */
    override val mention: String
        get() = if (position == 0) "@everyone" else "<@&$id>"

    /**
     * Edit this role
     */
    suspend fun edit(block: RoleUpdater.() -> Unit)
}