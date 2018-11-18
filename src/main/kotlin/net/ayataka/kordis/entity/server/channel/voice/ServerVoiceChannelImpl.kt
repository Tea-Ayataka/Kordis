package net.ayataka.kordis.entity.server.channel.voice

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.asLongOrNull
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.isNotEmpty
import net.ayataka.kordis.utils.json

class ServerVoiceChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerVoiceChannel, ServerChannelImpl(server, client, json["id"].asLong) {
    @Volatile override var bitrate: Int = -1
    @Volatile override var userLimit: Int = -1
    @Volatile override var category: ChannelCategory? = null

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        position = json["position"].asInt
        bitrate = json["bitrate"].asInt
        userLimit = json["user_limit"].asInt

        json.getOrNull("parent_id")?.asLongOrNull?.let {
            category = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerVoiceChannelImpl(name=$name, position=$position, bitrate=$bitrate, userLimit=$userLimit, category=$category)"
    }

    override suspend fun edit(block: ServerVoiceChannelBuilder.() -> Unit) {
        checkExistence()
        checkPermission(server, Permission.MANAGE_CHANNELS)
        checkManageable(this)
        val updater = ServerVoiceChannelBuilder(this).apply(block)

        val json = json {
            if (updater.name != name) {
                "name" to updater.name
            }

            if (updater.position != position) {
                "position" to updater.position
            }

            if (updater.bitrate != bitrate) {
                "bitrate" to updater.bitrate
            }

            if (updater.userLimit != userLimit) {
                "user_limit" to updater.userLimit
            }

            if (updater.category != category) {
                "parent_id" to updater.category?.id
            }

            if (updater.userPermissionOverwrites != userPermissionOverwrites
                    || updater.rolePermissionOverwrites != rolePermissionOverwrites) {
                "permission_overwrites" to permissionOverwritesToJson(updater)
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
        if (server.voiceChannels.find(id) == null) {
            throw NotFoundException()
        }
    }
}