package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import java.time.Instant

class MemberImpl(client: DiscordClient, json: JsonObject, override val server: Server, val user: User) : Member, DiscordEntity(client, user.id) {
    override val joinedAt = Instant.now()
    override val roles: List<Role> = mutableListOf()
    override val name = user.name
    override val discriminator = user.discriminator

    init {

    }

    fun updateStatus() {

    }
}