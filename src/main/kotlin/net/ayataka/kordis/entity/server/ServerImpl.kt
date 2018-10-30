package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.user.User

class ServerImpl(override val client: DiscordClient, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    override val members: List<User>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    private var _name: String? = null

    override val name get() = _name!!
}