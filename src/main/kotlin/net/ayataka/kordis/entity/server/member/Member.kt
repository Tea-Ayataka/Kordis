package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.rest.Endpoint
import java.time.Instant

interface Member : User {
    /**
     * The server this member in
     */
    val server: Server

    /**
     * The nickname of this member
     */
    val nickname: String?

    /**
     * When this member joined to the server
     */
    val joinedAt: Instant

    /**
     * The roles this member has
     */
    val roles: Collection<Role>

    /**
     * Whether this member is the server owner or not
     */
    val owner: Boolean
        get() = server.owner == this

    /**
     * Checks if this member has the permissions or not
     */
    fun has(vararg permissions: Permission): Boolean {
        return permissions().containsAll(permissions.toSet())
    }

    /**
     * The permissions this member has
     */
    fun permissions(): Collection<Permission> {
        return roles.map { it.permissions }.flatten().toSet()
    }

    /**
     * Checks if this member can manage (ban, kick, etc) the member or not
     */
    fun canManage(member: Member): Boolean {
        return member != this && !member.owner && member.roles.minBy { it.position }!!.position > roles.minBy { it.position }!!.position
    }
}