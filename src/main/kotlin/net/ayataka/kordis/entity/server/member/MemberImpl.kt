package net.ayataka.kordis.entity.server.member

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import java.time.Instant
import java.time.format.DateTimeFormatter

class MemberImpl(client: DiscordClientImpl, json: JsonObject, override val server: Server, val user: User) : Member, Updatable, DiscordEntity(client, user.id) {
    override val avatar = user.avatar
    override val name = user.name
    override val discriminator = user.discriminator

    @Volatile override var nickname: String? = null
    @Volatile override var joinedAt = Instant.now()!!
    @Volatile override var roles = mutableSetOf<Role>()

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        joinedAt = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json["joined_at"].content))
        roles = json["roles"].jsonArray.mapNotNull { server.roles.find(it.long) }.toMutableSet()
        nickname = json.getOrNull("nick")?.content

        // Update the user
        client.users.update(json["user"].jsonObject["id"].long, json["user"].jsonObject)
    }

    override fun toString(): String {
        return "Member(id=${user.id}, server=$server, user=${user.tag}, joinedAt=$joinedAt, roles=${roles.joinToString()})"
    }
}