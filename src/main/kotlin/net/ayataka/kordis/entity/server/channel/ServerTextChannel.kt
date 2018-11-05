package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.EmbedBuilder
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.rest.Endpoint

interface ServerTextChannel : ServerChannel, TextChannel {
    /**
     * The name of this text channel
     */
    override val name: String
    override val server: Server
    override val position: Int

    val topic: String?
    val isNsfw: Boolean
    val rateLimitPerUser: Int
    val category: Category?

    override suspend fun send(text: String): Message {
        return send(
                MessageBuilder().apply {
                    content(text)
                }.build()
        )
    }

    override suspend fun send(block: EmbedBuilder.() -> Unit): Message {
        val builder = EmbedBuilder()
        block(builder)

        return send(
                MessageBuilder().apply {
                    embed(builder.build())
                }.build()
        )
    }

    private suspend fun send(json: JsonObject): Message {
        val client = client as DiscordClientImpl
        val response = client.rest.request(
                Endpoint.CREATE_MESSAGE.format(mapOf("channel.id" to id)),
                json
        )

        return MessageImpl(client, response)
    }
}