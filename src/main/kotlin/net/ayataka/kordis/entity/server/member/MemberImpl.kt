package net.ayataka.kordis.entity.server.member

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.*
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.channel.PrivateTextChannelImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.PrivateMessageBlockedException
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
    @Volatile override var joinedAt = Instant.MIN!!
    @Volatile override var status = UserStatus.OFFLINE
    @Volatile override var roles = mutableSetOf<Role>()

    @Volatile private var privateChannelId = -1L

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        json.getOrNull("nick")?.let { nickname = it.contentOrNull }
        json.getOrNull("joined_at")?.let { joinedAt = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(it.content)) }
        roles = json["roles"].jsonArray.mapNotNull { server.roles.find(it.long) }.plus(server.roles.everyone).toMutableSet()

        // Update the user
        json.getObjectOrNull("user")?.let {
            client.users.update(it["id"].long, it)
        }
    }

    fun updatePresence(json: JsonObject) {
        json.getOrNull("roles")?.let {
            roles = it.jsonArray.mapNotNull { server.roles.find(it.long) }.plus(server.roles.everyone).toMutableSet()
        }

        status = UserStatus[json["status"].content]
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

    override suspend fun setNickname(name: String?) {
        if (this == server.members.botUser) {
            checkPermission(server, Permission.CHANGE_NICKNAME)
        } else {
            checkPermission(server, Permission.MANAGE_NICKNAMES)
            checkManageable(this)
        }

        client.rest.request(
                Endpoint.MODIFY_GUILD_MEMBER.format("guild.id" to server.id, "user.id" to id),
                json { "nick" to name }
        )
    }

    override suspend fun getPrivateChannel(): PrivateTextChannel {
        client.privateChannels.find(privateChannelId)?.let { return it }

        try {
            val response = client.rest.request(
                    Endpoint.CREATE_DM.format(),
                    json { "recipient_id" to id }
            ).jsonObject

            privateChannelId = response["id"].long
            return client.privateChannels.getOrPut(privateChannelId) { PrivateTextChannelImpl(client, response) }
        } catch (ex: DiscordException) {
            // FORBIDDEN
            if (ex.code == 403) {
                throw PrivateMessageBlockedException(this.toString())
            }

            throw ex
        }
    }

    override fun toString(): String {
        return "Member(id=${user.id}, server=$server, user=${user.tag}, joinedAt=$joinedAt, roles=${roles.joinToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is User && other !is Member) return (other as Entity).id == id
        if (other !is MemberImpl) return false
        if (!super.equals(other)) return false

        if (server != other.server) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + server.hashCode()
        result = 31 * result + user.hashCode()
        return result
    }
}