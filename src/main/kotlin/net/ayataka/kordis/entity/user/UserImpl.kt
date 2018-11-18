package net.ayataka.kordis.entity.user

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.image.ImageImpl
import net.ayataka.kordis.utils.asStringOrNull
import net.ayataka.kordis.utils.getOrNull

class UserImpl(client: DiscordClientImpl, json: JsonObject) : User, Updatable, DiscordEntity(client, json["id"].asLong) {
    @Volatile override var bot = false
    @Volatile override var avatar: Image? = null
    @Volatile override var name = ""
    @Volatile override var discriminator = ""

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["username"].asString
        discriminator = json["discriminator"].asString
        json["avatar"].asStringOrNull?.let { avatar = ImageImpl.avatar(id, it) }
        json.getOrNull("bot")?.let { bot = it.asBoolean }
    }

    override fun toString(): String {
        return "User(id='$id', name='$name', discriminator='$discriminator', bot=$bot')"
    }
}