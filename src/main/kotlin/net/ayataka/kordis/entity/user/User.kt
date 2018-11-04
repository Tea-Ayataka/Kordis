package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity

interface User : Entity {
    val avatarId: String
    val name: String
    val discriminator: String

    val tag: String
        get() = "$name#$discriminator"
    val mention: String
        get() = "<@$id>"
}