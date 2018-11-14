package net.ayataka.kordis.entity.server.ban

import net.ayataka.kordis.entity.user.User

interface Ban {
    val reason: String?
    val user: User
}