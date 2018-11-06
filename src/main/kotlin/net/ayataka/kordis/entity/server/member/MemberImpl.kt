package net.ayataka.kordis.entity.server.member

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import java.time.Instant
import java.time.format.DateTimeFormatter

class MemberImpl(client: DiscordClientImpl, json: JsonObject, override val server: Server, val user: User) : Member, DiscordEntity(client, user.id) {
    override val avatarId get() = user.avatarId
    override val name = user.name
    override val discriminator = user.discriminator

    override var joinedAt = Instant.now()!!
    override var roles = mutableSetOf<Role>()

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        joinedAt = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json["joined_at"].content))
        roles = json["roles"].jsonArray.mapNotNull { server.roles.find(it.long) }.toMutableSet()
    }

    override fun toString(): String {
        return "Member(id=${user.id}, server=$server, user=${user.tag}, joinedAt=$joinedAt, roles=${roles.joinToString()})"
    }
}