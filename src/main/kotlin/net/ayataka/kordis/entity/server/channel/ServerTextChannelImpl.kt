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
        try {
            update(json)
        } catch (ex: Exception) {
            ex.printStackTrace()
            println(json)
            throw ex
        }
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        topic = json.getOrNull("topic")?.content
        isNsfw = json.getOrNull("nsfw")?.boolean == true
        rateLimitPerUser = json.getOrNull("rate_limit_per_user")?.int ?: 0
        position = json["position"].int

        json.getOrNull("parent_id")?.long?.let {
            category = server.categories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerTextChannelImpl(id=$id, server=$server, name='$name', topic=$topic, isNsfw=$isNsfw, rateLimitPerUser=$rateLimitPerUser, position=$position, category=$category, userPermissionOverwrites=$userPermissionOverwrites, rolePermissionOverwrites=$rolePermissionOverwrites)"
    }
}