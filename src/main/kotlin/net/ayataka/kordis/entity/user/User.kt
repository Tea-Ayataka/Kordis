package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable

interface User : Mentionable, Nameable, Entity {
    val avatarId: String
    override val name: String
    val discriminator: String

    override val mention: String
        get() = "<@$id>"

    val tag: String
        get() = "$name#$discriminator"
}