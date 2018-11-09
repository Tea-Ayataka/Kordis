package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server

class ServerTextChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerTextChannel, ServerChannelImpl(server, client, json["id"].long) {
    @Volatile override var topic: String = ""
    @Volatile override var nsfw = false
    @Volatile override var rateLimitPerUser = -1
    @Volatile override var channelCategory: ChannelCategory? = null

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
        topic = json.getOrNull("topic")?.content ?: ""
        nsfw = json.getOrNull("nsfw")?.boolean == true
        rateLimitPerUser = json.getOrNull("rate_limit_per_user")?.int ?: 0
        position = json["position"].int

        json.getOrNull("parent_id")?.longOrNull?.let {
            channelCategory = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "ServerTextChannelImpl(id=$id, server=$server, name='$name', topic=$topic, nsfw=$nsfw, rateLimitPerUser=$rateLimitPerUser, position=$position, channelCategory=$channelCategory, userPermissionOverwrites=$userPermissionOverwrites, rolePermissionOverwrites=$rolePermissionOverwrites)"
    }
}