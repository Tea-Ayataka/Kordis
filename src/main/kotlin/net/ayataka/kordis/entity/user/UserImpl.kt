package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity

class UserImpl(client: DiscordClient, json: JsonObject) : User, DiscordEntity(client, json["id"].long) {
    override var avatarId = ""
    override var name = ""
    override var discriminator = ""

    init {
        update(json)

        synchronized(client.users) {
            if (client.users[id] != null) {
                throw IllegalStateException("The user is already initialized")
            }
            client.users[id] = this
        }
    }

    fun update(json: JsonObject) {
        name = json["username"].content
        discriminator = json["discriminator"].content
        avatarId = json["avatar"].content
    }

    override fun toString(): String {
        return "UserImpl(Id='$id', name='$name', discriminator=$discriminator)"
    }
}