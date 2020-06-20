package net.ayataka.kordis.entity.server.emoji

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.image.ImageImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.json

class EmojiImpl(
        override val server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : Emoji, Updatable, DiscordEntity(client, json["id"].asLong) {
    @Volatile override var name = ""
    @Volatile override var image = ImageImpl.emoji(id)

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
    }

    override suspend fun delete() {
        checkExistence()

        client.rest.request(
                Endpoint.DELETE_GUILD_EMOJI(server.id, id)
        )
    }

    override suspend fun edit(name: String) {
        checkExistence()

        client.rest.request(
                Endpoint.MODIFY_GUILD_EMOJI(server.id, id),
                json { "name" to name }
        )
    }

    private fun checkExistence() {
        if (server.emojis.find(id) == null) {
            throw NotFoundException()
        }
    }
}