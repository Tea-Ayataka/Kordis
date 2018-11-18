package net.ayataka.kordis.entity.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.exception.PrivateMessageBlockedException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.json

class PrivateTextChannelImpl(
        client: DiscordClientImpl,
        json: JsonObject
) : PrivateTextChannel, Updatable, DiscordEntity(client, json["id"].asLong) {
    @Volatile override var owner: User? = null
    override val recipients = mutableSetOf<User>()

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        recipients.clear()
        recipients.addAll(json["recipients"].asJsonArray.map {
            client.users.getOrPut(it.asJsonObject["id"].asLong) { UserImpl(client, it.asJsonObject) }
        })

        owner = json.getOrNull("owner_id")?.let { client.users.find(it.asLong) }
    }

    override suspend fun send(text: String): Message {
        return send {
            content = text
        }
    }

    override suspend fun send(block: MessageBuilder.() -> Unit): Message {
        try {
            val response = client.rest.request(
                    Endpoint.CREATE_MESSAGE.format("channel.id" to id),
                    MessageBuilder().apply(block).build()
            )

            return MessageImpl(client, response.asJsonObject)
        } catch (ex: DiscordException) {
            // FORBIDDEN
            if (ex.code == 403) {
                throw PrivateMessageBlockedException(this.toString())
            }

            throw ex
        }
    }

    override suspend fun getMessage(messageId: Long): Message? {
        return try {
            val response = client.rest.request(
                    Endpoint.GET_CHANNEL_MESSAGE.format("channel.id" to id, "message.id" to messageId)
            )

            MessageImpl(client, response.asJsonObject)
        } catch (ex: NotFoundException) {
            null
        }
    }

    override suspend fun getMessages(limit: Int): Collection<Message> {
        if (limit !in 1..100) {
            throw IllegalArgumentException("limit must be between 1 and 100")
        }

        val response = client.rest.request(
                Endpoint.GET_CHANNEL_MESSAGES.format("channel.id" to id),
                json { "limit" to limit }
        )

        return response.asJsonArray.map { MessageImpl(client, it.asJsonObject) }
    }

    override suspend fun deleteMessage(messageId: Long) {
        client.rest.request(
                Endpoint.DELETE_MESSAGE.format("channel.id" to id, "message.id" to messageId)
        )
    }

    override fun toString(): String {
        return "PrivateTextChannel(owner=$owner, recipients=$recipients)"
    }
}