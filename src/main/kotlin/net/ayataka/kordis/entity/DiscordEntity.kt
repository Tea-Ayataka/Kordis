package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClientImpl

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