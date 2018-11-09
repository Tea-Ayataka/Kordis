package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.updater.ServerChannelUpdater
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.rest.Endpoint

class ChannelCategoryImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ChannelCategory, ServerChannelImpl(server, client, json["id"].long) {
    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        position = json["position"].int
        loadPermissionOverwrites(json)
    }

    override suspend fun edit(block: ServerChannelUpdater.() -> Unit) {
        checkPermission(server, Permission.MANAGE_CHANNELS)

        val updater = ServerChannelUpdater(this).apply(block)

        val json = json {
            if (updater.name != name) {
                "name" to updater.name
            }

            if (updater.position != position) {
                "position" to updater.position
            }

            if (updater.userPermissionOverwrites != userPermissionOverwrites
                    || updater.rolePermissionOverwrites != rolePermissionOverwrites) {
                "permission_overwrites" to jsonArray {
                    updater.userPermissionOverwrites.forEach {
                        +json {
                            "id" to it.user.id
                            "allow" to it.allow
                            "deny" to it.deny
                            "type" to "user"
                        }
                    }

                    updater.rolePermissionOverwrites.forEach {
                        +json {
                            "id" to it.role.id
                            "allow" to it.allow
                            "deny" to it.deny
                            "type" to "role"
                        }
                    }
                }
            }
        }

        if (json.isNotEmpty()) {
            client.rest.request(
                    Endpoint.MODIFY_CHANNEL_PATCH.format("channel.id" to id),
                    json
            )
        }
    }
}