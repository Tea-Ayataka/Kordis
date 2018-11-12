package net.ayataka.kordis.entity.server.member

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.collection.everyone
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.rest.Endpoint
import java.time.Instant
import java.time.format.DateTimeFormatter

class MemberImpl(
        client: DiscordClientImpl,
        json: JsonObject,
        override val server: Server,
        val user: User
) : Member, Updatable, DiscordEntity(client, user.id) {
    override val bot get() = user.bot
    override val avatar get() = user.avatar
    override val name get() = user.name
    override val discriminator get() = user.discriminator

    @Volatile override var nickname: String? = null
    @Volatile override var joinedAt = Instant.now()!!
    @Volatile override var status = MemberStatus.OFFLINE
    @Volatile override var roles = mutableSetOf<Role>()

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        nickname = json.getOrNull("nick")?.content
        joinedAt = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json["joined_at"].content))
        roles = json["roles"].jsonArray.mapNotNull { server.roles.find(it.long) }.plus(server.roles.everyone).toMutableSet()

        // Update the user
        client.users.update(json["user"].jsonObject["id"].long, json["user"].jsonObject)
    }

    fun updatePresence(json: JsonObject) {
        roles = json["roles"].jsonArray.mapNotNull { server.roles.find(it.long) }.plus(server.roles.everyone).toMutableSet()
        status = MemberStatus[json["status"].content]
    }

    override fun toString(): String {
        return "Member(id=${user.id}, server=$server, user=${user.tag}, joinedAt=$joinedAt, roles=${roles.joinToString()})"
    }

    override suspend fun addRole(role: Role) {
        checkPermission(server, Permission.MANAGE_ROLES)
        checkManageable(role)

        client.rest.request(
                Endpoint.ADD_GUILD_MEMBER_ROLE.format(
                        "guild.id" to server.id,
                        "user.id" to id,
                        "role.id" to role.id
                )
        )
    }

    override suspend fun removeRole(role: Role) {
        checkPermission(server, Permission.MANAGE_ROLES)
        checkManageable(role)

        client.rest.request(
                Endpoint.REMOVE_GUILD_MEMBER_ROLE.format(
                        "guild.id" to server.id,
                        "user.id" to id,
                        "role.id" to role.id
                )
        )
    }
}