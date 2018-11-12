package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.collection.find
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.permission.Permission
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
        if (server.members.find(client.botUser)?.hasPermission(permission) != true) {
            throw MissingPermissionsException(server, "Permission: ${permission.desciption}")
        }
    }

    fun checkManageable(member: Member) {
        if (member.server.members.find(client.botUser)?.canManage(member) != true) {
            throw MissingPermissionsException(member.server, "User: ${member.tag} (${member.id})")
        }
    }

    fun checkManageable(role: Role) {
        if (role.server.members.find(client.botUser)?.canManage(role) != true) {
            throw MissingPermissionsException(role.server, "Role: ${role.name} (${role.id})")
        }
    }

    fun checkManageable(channel: ServerChannel) {
        if (channel.server.members.find(client.botUser)?.canManage(channel) != true) {
            throw MissingPermissionsException(channel.server, "Channel: ${channel.name} (${channel.id})")
        }
    }

    fun checkAccess(channel: ServerChannel) {
        if (channel.server.members.find(client.botUser)?.canAccess(channel) != true) {
            throw MissingPermissionsException(channel.server, "Channel: ${channel.name} (${channel.id})")
        }
    }

    override fun hashCode() = id.hashCode()
    override fun toString() = "DiscordEntity (id: $id)"
}