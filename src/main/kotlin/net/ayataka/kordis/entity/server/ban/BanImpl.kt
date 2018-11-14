package net.ayataka.kordis.entity.server.ban

import net.ayataka.kordis.entity.user.User

data class BanImpl(override val reason: String?, override val user: User) : Ban