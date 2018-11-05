package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable

interface User : Nameable, Entity {
    val avatarId: String
    override val name: String
    val discriminator: String

    val tag: String
        get() = "$name#$discriminator"
    val mention: String
        get() = "<@$id>"
}