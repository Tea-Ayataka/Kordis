package net.ayataka.kordis.entity.server.channel.text

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.rest.Endpoint

class ServerTextChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerTextChannel, ServerChannelImpl(server, client, json["id"].long) {
    @Volatile override var topic: String = ""
    @Volatile override var nsfw = false
    @Volatile override var rateLimitPerUser = -1
    @Volatile override var category: ChannelCategory? = null

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].content
        topic = json.getOrNull("topic")?.content ?: ""
        nsfw = json.getOrNull("nsfw")?.boolean == true
        rateLimitPerUser = json.getOrNull("rate_limit_per_user")?.int ?: 0
        position = json["position"].int

        json.getOrNull("parent_id")?.longOrNull?.let {
            category = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerTextChannelImpl(id=$id, server=$server, name='$name', topic=$topic, nsfw=$nsfw, rateLimitPerUser=$rateLimitPerUser, position=$position, category=$category, userPermissionOverwrites=$userPermissionOverwrites, rolePermissionOverwrites=$rolePermissionOverwrites)"
    }

    override suspend fun send(text: String): Message {
        return sendMessage {
            content = text
        }
    }

    override suspend fun send(block: EmbedBuilder.() -> Unit): Message {
        return sendMessage {
            embed = EmbedBuilder().apply(block).build().toJson()
        }
    }

    override suspend fun edit(block: ServerTextChannelBuilder.() -> Unit) {
        checkPermission(server, Permission.MANAGE_CHANNELS)
        checkManageable(this)

        val updater = ServerTextChannelBuilder(this).apply(block)

        val json = json {
            if (updater.name != name) {
                "name" to updater.name
            }

            if (updater.position != position) {
                "position" to updater.position
            }

            if (updater.topic != topic) {
                "topic" to updater.topic
            }

            if (updater.nsfw != nsfw) {
                "nsfw" to updater.nsfw
            }

            if (updater.rateLimitPerUser != rateLimitPerUser) {
                "rate_limit_per_user" to updater.rateLimitPerUser
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

    private suspend fun sendMessage(block: MessageBuilder.() -> Unit): Message {
        val response = client.rest.request(
                Endpoint.CREATE_MESSAGE.format("channel.id" to id),
                MessageBuilder().apply(block).build()
        )

        return MessageImpl(client, response.jsonObject, server)
    }
}