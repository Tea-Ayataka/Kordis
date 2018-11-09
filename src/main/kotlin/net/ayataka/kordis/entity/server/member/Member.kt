package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.user.User
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
    val isOwner: Boolean
        get() = server.owner == this

    /**
     * Whether this member is the server owner or not
     */
    val isAdmin: Boolean
        get() = Permission.ADMINISTRATOR in permissions

    /**
     * The permissions this member has
     */
    val permissions
        get() = roles.map { it.permissions }.flatten().toSet()

    /**
     * Checks if this member has the permissions or not
     */
    fun hasPermission(perm: Permission) = isAdmin || perm in permissions

    /**
     * Checks if this member can manage (ban, kick, etc) the member or not
     */
    fun canManage(member: Member) =
            member != this && !member.isOwner
                    && member.roles.maxBy { it.position }!!.position < roles.maxBy { it.position }!!.position

    /**
     * Checks if this member can manage the role or not
     */
    fun canManage(role: Role) = role.position < roles.maxBy { it.position }!!.position

    /**
     * Checks if this member can manage the channel or not
     */
    // TODO: MAKE THIS SMARTER
    fun canManage(channel: ServerChannel): Boolean {
        var result = true

        channel.rolePermissionOverwrites
                .filter { it.role in roles }
                .sortedBy { it.role.position }
                .forEach {
                    if (Permission.VIEW_CHANNEL in it.deny) {
                        result = false
                    } else if (Permission.VIEW_CHANNEL in it.allow) {
                        result = true
                    }
                }

        channel.userPermissionOverwrites
                .filter { it.user.id == id }
                .forEach {
                    if (Permission.VIEW_CHANNEL in it.deny) {
                        result = false
                    } else if (Permission.VIEW_CHANNEL in it.allow) {
                        result = true
                    }
                }

        if (result) {
            channel.rolePermissionOverwrites
                    .filter { it.role in roles }
                    .sortedBy { it.role.position }
                    .forEach {
                        if (Permission.MANAGE_CHANNELS in it.deny) {
                            result = false
                        } else if (Permission.MANAGE_CHANNELS in it.allow) {
                            result = true
                        }
                    }

            channel.userPermissionOverwrites
                    .filter { it.user.id == id }
                    .forEach {
                        if (Permission.MANAGE_CHANNELS in it.deny) {
                            result = false
                        } else if (Permission.MANAGE_CHANNELS in it.allow) {
                            result = true
                        }
                    }
        }

        return result
    }

    /**
     * Add a role to the member
     */
    suspend fun addRole(role: Role)

    /**
     * Remove a role from the member
     */
    suspend fun removeRole(role: Role)
}