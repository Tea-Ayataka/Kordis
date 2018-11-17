package net.ayataka.kordis.entity.server.emoji

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.json
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.image.ImageImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint

class EmojiImpl(
        override val server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : Emoji, Updatable, DiscordEntity(client, json["id"].long) {
    @Volatile override var name = ""
    @Volatile override var image = ImageImpl.emoji(id)

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].content
    }

    override suspend fun delete() {
        checkExistence()
        checkPermission(server, Permission.MANAGE_EMOJIS)

        client.rest.request(
                Endpoint.DELETE_GUILD_EMOJI.format("guild.id" to server.id, "emoji.id" to id)
        )
    }

    override suspend fun edit(name: String) {
        checkExistence()
        checkPermission(server, Permission.MANAGE_EMOJIS)

        client.rest.request(
                Endpoint.MODIFY_GUILD_EMOJI.format("guild.id" to server.id, "emoji.id" to id),
                json { "name" to name }
        )
    }

    private fun checkExistence() {
        if (server.emojis.find(id) == null) {
            throw NotFoundException()
        }
    }
}