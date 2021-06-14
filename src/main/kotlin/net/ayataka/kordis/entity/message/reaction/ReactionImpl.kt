package net.ayataka.kordis.entity.message.reaction

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.emoji.PartialEmoji
import net.ayataka.kordis.entity.server.emoji.PartialEmojiImpl
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.utils.asLongOrNull
import net.ayataka.kordis.utils.getOrNull

/**
 * Implementation of [Reaction]
 */
class ReactionImpl(
    client: DiscordClientImpl, json: JsonObject, _server: Server? = null
) : Reaction {
    override val server: Server? = _server ?: json.getOrNull("guild_id")?.let { client.servers.find(it.asLong) }
    override val userId: Long = json["user_id"].asLong
    override val channelId: Long = json["channel_id"].asLong
    override val messageId: Long = json["message_id"].asLong
    override val author: User?
    override val member: Member?
    override val emoji: PartialEmoji

    init {
        author = if (!json.has("webhook_id")) {
            json.getAsJsonObject("member")?.run {
                val authorData = getAsJsonObject("user")
                val authorId = authorData["id"].asLong
                client.users.updateOrPut(authorId, authorData) { UserImpl(client, authorData) }
            }
        } else null

        member = if (server != null && author != null && json.has("member")) {
            MemberImpl(client, json["member"].asJsonObject, server, author)
        } else null

        json["emoji"].asJsonObject.also {
            emoji = PartialEmojiImpl(id = it["id"].asLongOrNull, name = it["name"].asString)
        }
    }
}
