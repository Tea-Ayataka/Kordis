package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.image.Icon
import net.ayataka.kordis.entity.image.IconImpl

class UserImpl(client: DiscordClientImpl, json: JsonObject) : User, Updatable, DiscordEntity(client, json["id"].long) {
    @Volatile override var bot = false
    @Volatile override var avatar: Icon? = null
    @Volatile override var name = ""
    @Volatile override var discriminator = ""

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["username"].content
        discriminator = json["discriminator"].content
        avatar = IconImpl.avatar(id, json["avatar"].content)
        json.getOrNull("bot")?.let { bot = it.boolean }
    }

    override fun toString(): String {
        return "User(Id='$id', name='$name', discriminator='$discriminator')"
    }
}