package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.embed.Embed
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.message.embed.EmbedImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.rest.Endpoint
import java.time.Instant
import java.time.format.DateTimeFormatter

@Suppress("JoinDeclarationAndAssignment")
class MessageImpl(client: DiscordClientImpl, json: JsonObject, _server: Server? = null) : Message, DiscordEntity(client, json["id"].long) {
    override val type: MessageType
    override val server: Server?
    override val mentionsEveryone: Boolean
    override val pinned: Boolean
    override var author: User? = null
    override val channel: TextChannel
    override val content: String
    override val embeds: List<Embed>
    override val tts: Boolean
    override val timestamp: Instant
    override val editedTimestamp: Instant?

    init {
        type = MessageType[json["type"].int]
        mentionsEveryone = json["mention_everyone"].boolean
        pinned = json["pinned"].boolean
        content = json["content"].content
        tts = json["tts"].boolean
        embeds = json.getArrayOrNull("embed")?.map { EmbedImpl(it.jsonObject) } ?: emptyList()
        timestamp = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json["timestamp"].content))
        editedTimestamp = json.getOrNull("edited_timestamp")?.contentOrNull
                ?.let { Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(it)) }

        server = _server ?: json.getOrNull("guild_id")?.let { client.servers.find(it.long) }
        channel = server?.textChannels?.find(json["channel_id"].long) ?: throw IllegalStateException()

        if (!json.containsKey("webhook_id")) {
            val authorData = json["author"].jsonObject
            val authorId = authorData["id"].long

            author = client.users.updateOrPut(authorId, authorData) { UserImpl(client, authorData) }
        }
    }

    override suspend fun edit(text: String, embed: (EmbedBuilder.() -> Unit)?): Message {
        val response = client.rest.request(
                Endpoint.EDIT_MESSAGE.format("message.id" to id, "channel.id" to channel.id),
                json {
                    if (content.isNotEmpty()) {
                        "content" to text
                    }
                    if (embed != null) {
                        "embed" to EmbedBuilder().apply(embed).build().toJson()
                    }
                }
        )

        return MessageImpl(client, response.jsonObject)
    }
}