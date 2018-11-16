package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.user.User
import java.time.Instant

interface Member : User {
    /**
     * The server the member in
     */
    val server: Server

    /**
     * The nickname of the member
     */
    val nickname: String?

    /**
     * When the member joined to the server
     */
    val joinedAt: Instant

    /**
     * The online status of the member
     */
    val status: UserStatus

    /**
     * The roles the member has
     */
    val roles: Collection<Role>

    /**
     * Whether the member is the server owner
     */
    val isOwner: Boolean
        get() = server.owner == this

    /**
     * Whether the member is the server owner
     */
    val isAdmin: Boolean
        get() = isOwner || Permission.ADMINISTRATOR in permissions

    /**
     * The permissions the member has
     */
    val permissions
        get() = roles.map { it.permissions }.flatten().toSet()

    /**
     * Checks if the member has the permissions
     */
    fun hasPermission(perm: Permission) = isAdmin || perm in permissions

    /**
     * Checks if the member can manage (ban, kick, etc) the member
     */
    fun canManage(member: Member) =
            member != this && !member.isOwner
                    && member.roles.maxBy { it.position }!!.position < roles.maxBy { it.position }!!.position

    /**
     * Checks if the member can manage the role
     */
    fun canManage(role: Role) = role.position < roles.maxBy { it.position }!!.position

    /**
     * Checks if the member can manage the channel
     */
    fun canManage(channel: ServerChannel): Boolean {
        if (!canAccess(channel)) {
            return false
        }

        var result = true
        channel.rolePermissionOverwrites
                .filter { it.role in roles }
                .sortedBy { it.role.position }
                .plus(channel.userPermissionOverwrites.filter { it.user == this })
                .forEach {
                    if (Permission.MANAGE_CHANNELS in it.deny) {
                        result = false
                    } else if (Permission.MANAGE_CHANNELS in it.allow) {
                        result = true
                    }
                }

        return result
    }

    /**
     * Checks if the member can access to the channel
     */
    fun canAccess(channel: ServerChannel): Boolean {
        var result = true

        channel.rolePermissionOverwrites
                .filter { it.role in roles }
                .sortedBy { it.role.position }
                .plus(channel.userPermissionOverwrites.filter { it.user == this })
                .forEach {
                    if (Permission.VIEW_CHANNEL in it.deny) {
                        result = false
                    } else if (Permission.VIEW_CHANNEL in it.allow) {
                        result = true
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

    /**
     * Kick the member from the server
     */
    suspend fun kick() {
        server.kick(this)
    }

    /**
     * Ban the member from the server
     */
    suspend fun ban(deleteMessageDays: Int = 0, reason: String? = null) {
        server.ban(this, deleteMessageDays, reason)
    }

    /**
     * Unban the member from the server
     */
    suspend fun unban() {
        server.unban(this)
    }

    /**
     * Change the member's nickname
     *
     * @param name A new nickname to set. specify null to clear.
     */
    suspend fun setNickname(name: String?)

    /**
     * Get the private channel for the member
     */
    suspend fun getPrivateChannel(): PrivateTextChannel
}