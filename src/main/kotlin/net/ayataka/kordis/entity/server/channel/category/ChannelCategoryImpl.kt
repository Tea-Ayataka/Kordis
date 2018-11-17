package net.ayataka.kordis.entity.server.channel.category

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint

class ChannelCategoryImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ChannelCategory, ServerChannelImpl(server, client, json["id"].long) {
    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].content
        position = json["position"].int
        loadPermissionOverwrites(json)
    }

    override suspend fun edit(block: ServerChannelBuilder.() -> Unit) {
        checkExistence()
        checkPermission(server, Permission.MANAGE_CHANNELS)
        checkManageable(this)

        val builder = ServerChannelBuilder(this).apply(block)

        val json = json {
            if (builder.name != name) {
                "name" to builder.name
            }

            if (builder.position != position) {
                "position" to builder.position
            }

            if (builder.userPermissionOverwrites != userPermissionOverwrites
                    || builder.rolePermissionOverwrites != rolePermissionOverwrites) {
                "permission_overwrites" to permissionOverwritesToJson(builder)
            }
        }

        if (json.isNotEmpty()) {
            client.rest.request(
                    Endpoint.MODIFY_CHANNEL_PATCH.format("channel.id" to id),
                    json
            )
        }
    }

    private fun checkExistence() {
        if (server.channelCategories.find(id) == null) {
            throw NotFoundException()
        }
    }
}