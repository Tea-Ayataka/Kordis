package net.ayataka.kordis.entity.server.channel.category

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.isNotEmpty
import net.ayataka.kordis.utils.json

class ChannelCategoryImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ChannelCategory, ServerChannelImpl(server, client, json["id"].asLong) {
    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        position = json["position"].asInt
        loadPermissionOverwrites(json)
    }

    override suspend fun edit(block: ServerChannelBuilder.() -> Unit) {
        checkExistence()

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
                    Endpoint.MODIFY_CHANNEL_PATCH(channel_id = id),
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