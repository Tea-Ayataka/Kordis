package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity

interface User : Entity {
    val name: String
    val discriminator: Int
    val tag: String
        get() = "$name#$discriminator"
    val mention: String
        get() = "<@$id>"
}