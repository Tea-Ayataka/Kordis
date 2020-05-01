package net.ayataka.kordis.entity.message

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.attachment.Attachment
import net.ayataka.kordis.entity.message.attachment.AttachmentImpl
import net.ayataka.kordis.entity.message.embed.Embed
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.message.embed.EmbedImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.emoji.PartialEmoji
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.*
import java.net.URLEncoder
import java.time.Instant
import java.time.format.DateTimeFormatter

@Suppress("JoinDeclarationAndAssignment")
class MessageImpl(client: DiscordClientImpl, json: JsonObject, _server: Server? = null) : Message, DiscordEntity(client, json["id"].asLong) {
    override val type: MessageType
    override val server: Server?
    override val mentionsEveryone: Boolean
    override val pinned: Boolean
    override val author: User?
    override val member: Member?
    override val channel: TextChannel
    override val content: String
    override val embeds: List<Embed>
    override val attachments: List<Attachment>
    override val tts: Boolean
    override val timestamp: Instant
    override val editedTimestamp: Instant?

    init {
        type = MessageType[json["type"].asInt]
        mentionsEveryone = json["mention_everyone"].asBoolean
        pinned = json["pinned"].asBoolean
        content = json["content"].asString
        tts = json["tts"].asBoolean
        embeds = json.getArrayOrNull("embeds")?.map { EmbedImpl(it.asJsonObject) } ?: emptyList()
        attachments = json.getArrayOrNull("attachments")?.map { AttachmentImpl(client, it.asJsonObject) } ?: emptyList()
        timestamp = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json["timestamp"].asString))
        editedTimestamp = json.getOrNull("edited_timestamp")?.asStringOrNull
                ?.let { Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(it)) }

        server = _server ?: json.getOrNull("guild_id")?.let { client.servers.find(it.asLong) }
        channel = server?.textChannels?.find(json["channel_id"].asLong)
                ?: client.privateChannels.find(json["channel_id"].asLong)
                        ?: throw IllegalStateException("unknown channel id received")

        author = if (!json.has("webhook_id")) {
            val authorData = json["author"].asJsonObject
            val authorId = authorData["id"].asLong

            client.users.updateOrPut(authorId, authorData) { UserImpl(client, authorData) }
        } else null

        member = if (server != null && author != null && json.has("member")) {
            MemberImpl(client, json["member"].asJsonObject, server, author)
        } else null
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

        return MessageImpl(client, response.asJsonObject, server)
    }

    override suspend fun getReactors(emoji: PartialEmoji): List<User> {
        val response = client.rest.request(Endpoint.GET_REACTIONS.format("channel.id" to channel.id,
                "message.id" to id, "emoji" to encodeEmoji(emoji)))

        return response.asJsonArray.map { UserImpl(client, it.asJsonObject) }
    }

    override suspend fun addReaction(emoji: PartialEmoji) {
        client.rest.request(Endpoint.CREATE_REACTION.format("channel.id" to channel.id, "message.id" to id,
                "emoji" to encodeEmoji(emoji)))
    }

    override suspend fun removeReaction(emoji: PartialEmoji, user: User) {
        client.rest.request(Endpoint.DELETE_USER_REACTION.format("channel.id" to channel.id, "message.id" to id,
                "emoji" to encodeEmoji(emoji), "user.id" to user.id))
    }

    override suspend fun removeReaction(emoji: PartialEmoji) {
        client.rest.request(Endpoint.DELETE_OWN_REACTION.format("channel.id" to channel.id, "message.id" to id,
                "emoji" to encodeEmoji(emoji)))
    }

    override suspend fun clearReactions() {
        client.rest.request(Endpoint.DELETE_ALL_REACTIONS.format("channel.id" to channel.id, "message.id" to id))
    }

    private fun encodeEmoji(emoji: PartialEmoji) =
            if (emoji.id == null)
                URLEncoder.encode(emoji.name, Charsets.UTF_8)
            else
                "${URLEncoder.encode(emoji.name, Charsets.UTF_8)}:${emoji.id}"

    override fun toString(): String {
        return "Message(" +
                "type=$type," +
                "server=$server," +
                "mentionsEveryone=$mentionsEveryone," +
                "pinned=$pinned," +
                "author=$author," +
                "channel=$channel," +
                "content='$content'," +
                "embeds=$embeds," +
                "tts=$tts," +
                "timestamp=$timestamp," +
                "editedTimestamp=$editedTimestamp" +
                ")"
    }
}