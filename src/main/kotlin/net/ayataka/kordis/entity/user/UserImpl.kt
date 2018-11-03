package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity

class UserImpl(client: DiscordClient, json: JsonObject) : User, DiscordEntity(client, json["id"].long) {
    override var avatarId = ""
    override var name = ""
    override var discriminator = -1

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["username"].content
        discriminator = json["discriminator"].int
        avatarId = json["avatar"].content
    }
}