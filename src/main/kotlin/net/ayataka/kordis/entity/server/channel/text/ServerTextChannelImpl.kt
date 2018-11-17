package net.ayataka.kordis.entity.server.channel.text

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint

class ServerTextChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerTextChannel, ServerChannelImpl(server, client, json["id"].long) {
    @Volatile override var topic: String? = null
    @Volatile override var nsfw = false
    @Volatile override var rateLimitPerUser = -1
    @Volatile override var category: ChannelCategory? = null

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].content
        topic = json.getOrNull("topic")?.contentOrNull
        nsfw = json.getOrNull("nsfw")?.boolean == true
        rateLimitPerUser = json.getOrNull("rate_limit_per_user")?.int ?: 0
        position = json["position"].int

        json.getOrNull("parent_id")?.longOrNull?.let {
            category = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerTextChannelImpl(id=$id, server=$server, name='$name', topic=$topic, nsfw=$nsfw, rateLimitPerUser=$rateLimitPerUser, position=$position, category=$category)"
    }

    override suspend fun send(text: String): Message {
        return send { content = text }
    }

    override suspend fun send(block: MessageBuilder.() -> Unit): Message {
        checkExistence()

        val response = client.rest.request(
                Endpoint.CREATE_MESSAGE.format("channel.id" to id),
                MessageBuilder().apply(block).build()
        )

        return MessageImpl(client, response.jsonObject, server)
    }

    override suspend fun edit(block: ServerTextChannelBuilder.() -> Unit) {
        checkExistence()
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

    override suspend fun getMessage(messageId: Long): Message? {
        checkExistence()

        return try {
            val response = client.rest.request(
                    Endpoint.GET_CHANNEL_MESSAGE.format("channel.id" to id, "message.id" to messageId)
            )

            MessageImpl(client, response.jsonObject, server)
        } catch (ex: NotFoundException) {
            null
        }
    }

    override suspend fun getMessages(limit: Int): Collection<Message> {
        checkExistence()
        checkPermission(server, Permission.READ_MESSAGE_HISTORY)
        checkAccess(this)

        if (limit !in 1..100) {
            throw IllegalArgumentException("limit must be between 1 and 100")
        }

        val response = client.rest.request(
                Endpoint.GET_CHANNEL_MESSAGES.format("channel.id" to id),
                json { "limit" to limit }
        )

        return response.jsonArray.map { MessageImpl(client, it.jsonObject, server) }
    }

    override suspend fun deleteMessage(messageId: Long) {
        checkExistence()
        checkAccess(this)

        client.rest.request(
                Endpoint.DELETE_MESSAGE.format("channel.id" to id, "message.id" to messageId)
        )
    }

    override suspend fun deleteMessages(messageIds: Collection<Long>) {
        checkExistence()
        checkAccess(this)

        if (messageIds.isEmpty()) {
            return
        }

        if (messageIds.size == 1) {
            deleteMessage(messageIds.first())
            return
        }

        messageIds.chunked(100).forEach {
            client.rest.request(
                    Endpoint.BULK_DELETE_MESSAGES.format("channel.id" to id),
                    json {
                        "messages" to jsonArray { it.forEach { +JsonPrimitive(it) } }
                    }
            )
        }
    }

    private fun checkExistence() {
        if (server.textChannels.find(id) == null) {
            throw NotFoundException()
        }
    }
}