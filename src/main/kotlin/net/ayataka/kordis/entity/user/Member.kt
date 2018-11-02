package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import java.time.Instant

interface Member : User {
    val server: Server
    val joinedAt: Instant
    val roles: List<Role>
}