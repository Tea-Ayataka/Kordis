package net.ayataka.kordis.entity.server.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.permission.overwrite.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.overwrite.UserPermissionOverwrite
import net.ayataka.kordis.entity.user.PartialUserImpl
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.json
import net.ayataka.kordis.utils.jsonArray

abstract class ServerChannelImpl(
        override val server: Server,
        client: DiscordClientImpl,
        id: Long
) : ServerChannel, Updatable, DiscordEntity(client, id) {
    @Volatile final override var name = ""
    @Volatile final override var position = -1
    final override val userPermissionOverwrites = mutableSetOf<UserPermissionOverwrite>()
    final override val rolePermissionOverwrites = mutableSetOf<RolePermissionOverwrite>()

    fun loadPermissionOverwrites(json: JsonObject) {
        userPermissionOverwrites.clear()
        rolePermissionOverwrites.clear()

        json["permission_overwrites"].asJsonArray.map { it.asJsonObject }.forEach {
            val type = it["type"].asString

            if (type == "0" || type == "role") {
                server.roles.find(it["id"].asLong)?.let { role ->
                    rolePermissionOverwrites.add(RolePermissionOverwrite(
                            role,
                            PermissionSet(it["allow"].asLong),
                            PermissionSet(it["deny"].asLong)
                    ))
                }

                return@forEach
            }

            if (type == "1" || type == "member") {
                userPermissionOverwrites.add(UserPermissionOverwrite(
                        PartialUserImpl(client, it["id"].asLong),
                        PermissionSet(it["allow"].asLong),
                        PermissionSet(it["deny"].asLong)
                ))
                return@forEach
            }
        }
    }

    override suspend fun delete() {
        client.rest.request(Endpoint.DELETE_CHANNEL.format("channel.id" to id))
    }

    companion object {
        fun permissionOverwritesToJson(builder: ServerChannelBuilder) = jsonArray {
            builder.rolePermissionOverwrites.forEach {
                +json {
                    "id" to it.role.id
                    "allow" to it.allow.compile()
                    "deny" to it.deny.compile()
                    "type" to 0
                }
            }

            builder.userPermissionOverwrites.forEach {
                +json {
                    "id" to it.user.id
                    "allow" to it.allow.compile()
                    "deny" to it.deny.compile()
                    "type" to 1
                }
            }
        }
    }
}