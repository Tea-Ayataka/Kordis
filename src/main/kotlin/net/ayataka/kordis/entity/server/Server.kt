package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.user.User

interface Server : Entity {
    val name: String
    val members: List<User>
}