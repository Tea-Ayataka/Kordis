package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.Permissionable
import net.ayataka.kordis.entity.image.Icon

interface User : Mentionable, Nameable, Permissionable, Entity {
    /**
     * Whether the user is bot or not
     */
    val bot: Boolean

    /**
     * The avatar image of the user
     */
    val avatar: Icon?

    /**
     * The discriminator of the user
     */
    val discriminator: String

    /**
     * The mention tag of the user
     */
    override val mention: String
        get() = "<@$id>"

    /**
     * The full tag of the user
     */
    val tag: String
        get() = "$name#$discriminator"
}