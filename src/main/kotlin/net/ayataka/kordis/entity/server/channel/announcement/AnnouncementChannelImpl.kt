package net.ayataka.kordis.entity.server.channel.announcement

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
import net.ayataka.kordis.utils.*

@Suppress("DuplicatedCode")
class AnnouncementChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : AnnouncementChannel, ServerChannelImpl(server, client, json["id"].asLong) {
    @Volatile
    override var topic: String? = null
    @Volatile
    override var nsfw = false
    @Volatile
    override var category: ChannelCategory? = null

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        topic = json.getOrNull("topic")?.asStringOrNull
        nsfw = json.getOrNull("nsfw")?.asBoolean == true
        position = json["position"].asInt

        json.getOrNull("parent_id")?.asLongOrNull?.let {
            category = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override suspend fun send(text: String): Message {
        return send { content = text }
    }

    override suspend fun send(block: MessageBuilder.() -> Unit): Message {
        checkExistence()

        val response = client.rest.request(
                Endpoint.CREATE_MESSAGE(channel_id = id),
                MessageBuilder().apply(block).build()
        )

        return MessageImpl(client, response.asJsonObject, server)
    }

    override suspend fun getMessage(messageId: Long): Message? {
        checkExistence()

        return try {
            val response = client.rest.request(
                    Endpoint.GET_CHANNEL_MESSAGE(channel_id = id, message_id = messageId)
            )

            MessageImpl(client, response.asJsonObject, server)
        } catch (ex: NotFoundException) {
            null
        }
    }

    override suspend fun getMessages(limit: Int): Collection<Message> {
        checkExistence()

        if (limit !in 1..100) {
            throw IllegalArgumentException("limit must be between 1 and 100")
        }

        val response = client.rest.request(
                Endpoint.GET_CHANNEL_MESSAGES(channel_id = id),
                json { "limit" to limit }
        )

        return response.asJsonArray.map { MessageImpl(client, it.asJsonObject, server) }
    }

    override suspend fun deleteMessage(messageId: Long) {
        checkExistence()

        client.rest.request(
                Endpoint.DELETE_MESSAGE(channel_id = id, message_id = messageId)
        )
    }

    override suspend fun deleteMessages(messageIds: Collection<Long>) {
        checkExistence()

        if (messageIds.isEmpty()) {
            return
        }

        if (messageIds.size == 1) {
            deleteMessage(messageIds.first())
            return
        }

        messageIds.chunked(100).forEach {
            client.rest.request(
                    Endpoint.BULK_DELETE_MESSAGES(channel_id = id),
                    json {
                        "messages" to jsonArray { it.forEach { +JsonPrimitive(it) } }
                    }
            )
        }
    }

    private fun checkExistence() {
        if (server.announcementChannels.find(id) == null) {
            throw NotFoundException()
        }
    }
}