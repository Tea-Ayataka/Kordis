package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server

class ServerTextChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerTextChannel, ServerChannelImpl(server, client, json["id"].long) {
    override var topic: String? = null
    override var isNsfw = false
    override var rateLimitPerUser = -1
    override var category: Category? = null

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        topic = json["topic"].contentOrNull
        isNsfw = json["nsfw"].boolean
        rateLimitPerUser = json["rate_limit_per_user"].int
        position = json["position"].int
        json["parent_id"].longOrNull?.let {
            category = server.categories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerTextChannelImpl(id=$id, server=$server, name='$name', topic=$topic, isNsfw=$isNsfw, rateLimitPerUser=$rateLimitPerUser, position=$position, category=$category, userPermissionOverwrites=$userPermissionOverwrites, rolePermissionOverwrites=$rolePermissionOverwrites)"
    }
}