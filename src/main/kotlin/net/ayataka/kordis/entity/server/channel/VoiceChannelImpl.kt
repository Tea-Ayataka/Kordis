package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server

class VoiceChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : ServerVoiceChannel, ServerChannelImpl(server, client, json["id"].long) {
    @Volatile override var bitrate: Int = -1
    @Volatile override var userLimit: Int = -1
    @Volatile override var category: Category? = null

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
        position = json["position"].int
        bitrate = json["bitrate"].int
        userLimit = json["user_limit"].int

        json.getOrNull("parent_id")?.longOrNull?.let {
            category = server.categories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    override fun toString(): String {
        return "VoiceChannelImpl(name=$name, position=$position, bitrate=$bitrate, userLimit=$userLimit, category=$category)"
    }
}