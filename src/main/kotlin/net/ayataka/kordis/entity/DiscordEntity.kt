package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.exception.MissingPermissionsException

abstract class DiscordEntity(
        override val client: DiscordClientImpl,
        override val id: Long
) : Entity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false

        return id == other.id
    }

    fun checkPermission(server: Server, permission: Permission) {
        val myself = server.members.find(client.botUser) ?: throw IllegalStateException()

        if (isNotInitialized(myself)) {
            return
        }

        if (!myself.hasPermission(permission)) {
            throw MissingPermissionsException(server, "Permission: ${permission.desciption}")
        }
    }

    fun checkManageable(member: Member) {
        val myself = member.server.members.find(client.botUser) ?: throw IllegalStateException()

        if (isNotInitialized(myself)) {
            return
        }

        if (!myself.canManage(member)) {
            throw MissingPermissionsException(member.server, "User: ${member.tag} (${member.id})")
        }
    }

    fun checkManageable(role: Role) {
        val myself = role.server.members.find(client.botUser) ?: throw IllegalStateException()

        if (isNotInitialized(myself)) {
            return
        }

        if (!myself.canManage(role)) {
            throw MissingPermissionsException(role.server, "Role: ${role.name} (${role.id})")
        }
    }

    fun checkManageable(channel: ServerChannel) {
        val myself = channel.server.members.find(client.botUser) ?: throw IllegalStateException()

        if (isNotInitialized(myself)) {
            return
        }

        if (channel.server.members.find(client.botUser)?.canManage(channel) != true) {
            throw MissingPermissionsException(channel.server, "Channel: ${channel.name} (${channel.id})")
        }
    }

    fun checkAccess(channel: ServerChannel) {
        val myself = channel.server.members.find(client.botUser) ?: throw IllegalStateException()

        if (isNotInitialized(myself)) {
            return
        }

        if (channel.server.members.find(client.botUser)?.canAccess(channel) != true) {
            throw MissingPermissionsException(channel.server, "Channel: ${channel.name} (${channel.id})")
        }
    }

    // Bot users can not have one or fewer roles. If so, this means the server roles are not initialized yet.
    private fun isNotInitialized(myself: Member) = myself.roles.size < 2

    override fun hashCode() = id.hashCode()
    override fun toString() = "DiscordEntity (id: $id)"
}