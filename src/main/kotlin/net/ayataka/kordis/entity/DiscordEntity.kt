package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.collection.find
import net.ayataka.kordis.entity.server.Server
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
        if (server.members.find(client.botUser)?.has(permission) != true) {
            throw MissingPermissionsException(server)
        }
    }

    fun checkManageable(member: Member) {
        if (member.server.members.find(client.botUser)?.canManage(member) != true) {
            throw MissingPermissionsException(member.server)
        }
    }

    override fun hashCode() = id.hashCode()
    override fun toString() = "DiscordEntity (id: $id)"
}