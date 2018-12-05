package net.ayataka.kordis.entity.user

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.channel.PrivateTextChannelImpl
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.image.ImageImpl
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.PrivateMessageBlockedException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.asStringOrNull
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.json

class UserImpl(client: DiscordClientImpl, json: JsonObject) : User, Updatable, DiscordEntity(client, json["id"].asLong) {
    @Volatile override var bot = false
    @Volatile override var avatar: Image? = null
    @Volatile override var name = ""
    @Volatile override var discriminator = ""

    @Volatile private var privateChannelId = -1L

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["username"].asString
        discriminator = json["discriminator"].asString
        json["avatar"].asStringOrNull?.let { avatar = ImageImpl.avatar(id, it) }
        json.getOrNull("bot")?.let { bot = it.asBoolean }
    }

    override suspend fun getPrivateChannel(): PrivateTextChannel {
        client.privateChannels.find(privateChannelId)?.let { return it }

        try {
            val response = client.rest.request(
                    Endpoint.CREATE_DM.format(),
                    json { "recipient_id" to id }
            ).asJsonObject

            privateChannelId = response["id"].asLong
            return client.privateChannels.getOrPut(privateChannelId) { PrivateTextChannelImpl(client, response) }
        } catch (ex: DiscordException) {
            // FORBIDDEN
            if (ex.code == 403) {
                throw PrivateMessageBlockedException(this.toString())
            }

            throw ex
        }
    }

    override fun toString(): String {
        return "User(id='$id', name='$name', discriminator='$discriminator', bot=$bot')"
    }
}