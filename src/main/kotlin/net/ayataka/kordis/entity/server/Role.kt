package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import java.awt.Color

interface Role : Mentionable, Nameable, Entity {
    /**
     * The name of this role
     */
    override val name: String

    /**
     * The allowed permissions of this role
     */
    val permissions: Int

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
    val isHoisted: Boolean

    /**
     * Whether this role is managed
     */
    val isManaged: Boolean

    /**
     * Whether this role is mentionable
     */
    val isMentionable: Boolean

    /**
     * The mention tag of this role
     */
    override val mention: String
        get() = "<@&$id>"

    fun edit(name: String? = null, permissions: Int? = null, color: Color? = null)
}