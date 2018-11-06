package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import java.time.Instant

interface Member : User {
    val server: Server
    val joinedAt: Instant
    val roles: Collection<Role>
}