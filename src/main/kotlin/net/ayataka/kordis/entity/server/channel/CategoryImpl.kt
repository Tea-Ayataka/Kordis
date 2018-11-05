package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server

class CategoryImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : Category, ServerChannelImpl(server, client, json["id"].long) {

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        position = json["position"].int
        loadPermissionOverwrites(json)
    }
}