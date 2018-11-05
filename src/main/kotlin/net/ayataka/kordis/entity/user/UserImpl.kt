package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity

class UserImpl(client: DiscordClientImpl, json: JsonObject) : User, DiscordEntity(client, json["id"].long) {
    override var avatarId = ""
    override var name = ""
    override var discriminator = ""

    init {
        update(json)

        synchronized(client.users) {
            if (!client.users.add(this)) {
                throw IllegalStateException("This user is already initialized")
            }
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