package net.ayataka.kordis.entity.server.channel

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.rest.Endpoint

interface ServerTextChannel : ServerChannel, Mentionable, TextChannel {
    val topic: String
    val nsfw: Boolean
    val rateLimitPerUser: Int
    val channelCategory: ChannelCategory?

    /**
     * The mention tag of this text channel
     */
    override val mention: String
        get() = "<#$id>"

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

    private suspend fun sendMessage(block: MessageBuilder.() -> Unit): Message {
        val client = client as DiscordClientImpl
        val response = client.rest.request(
                Endpoint.CREATE_MESSAGE.format(mapOf("channel.id" to id)),
                MessageBuilder().apply(block).build()
        )

        return MessageImpl(client, response, server)
    }
}