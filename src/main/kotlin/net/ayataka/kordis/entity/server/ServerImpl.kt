package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.user.Member

class ServerImpl(client: DiscordClient, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    override var name = ""
    override val members = mutableListOf<Member>()

    init {
        name = ""
    }
}