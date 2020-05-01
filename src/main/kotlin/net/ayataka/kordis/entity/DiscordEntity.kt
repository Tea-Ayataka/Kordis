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
    override fun hashCode() = id.hashCode()
    override fun toString() = "DiscordEntity (id: $id)"
}