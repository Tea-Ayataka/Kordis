package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.json
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.rest.Endpoint

class UserImpl(client: DiscordClientImpl, json: JsonObject) : User, Updatable, DiscordEntity(client, json["id"].long) {
    @Volatile override var avatarId = ""
    @Volatile override var name = ""
    @Volatile override var discriminator = ""

    init {
        update(json)

        synchronized(client.users) {
            if (!client.users.add(this)) {
                throw IllegalStateException("This user is already initialized")
            }
        }
    }

    override fun update(json: JsonObject) {
        name = json["username"].content
        discriminator = json["discriminator"].content
        avatarId = json["avatar"].content
    }

    override fun toString(): String {
        return "User(Id='$id', name='$name', discriminator=$discriminator)"
    }

    override suspend fun ban(server: Server, deleteMessageDays: Int, reason: String?)  {
        checkPermission(server, Permission.BAN_MEMBERS)
        server.members.find(id)?.let { checkManageable(it) }

        client.rest.request(
                Endpoint.CREATE_GUILD_BAN.format(mapOf("guild.id" to server.id, "user.id" to id)),
                json {
                    "delete-message-days" to deleteMessageDays
                    if (reason != null && reason.isNotEmpty()) {
                        "reason" to reason
                    }
                }
        )
    }

    override suspend fun unban(server: Server) {
        checkPermission(server, Permission.BAN_MEMBERS)

        client.rest.request(
                Endpoint.REMOVE_GUILD_BAN.format(mapOf("guild.id" to server.id, "user.id" to id))
        )
    }
}