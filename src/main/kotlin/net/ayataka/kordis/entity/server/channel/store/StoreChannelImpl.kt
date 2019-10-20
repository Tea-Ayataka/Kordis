package net.ayataka.kordis.entity.server.channel.store

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.utils.*

@Suppress("DuplicatedCode")
class StoreChannelImpl(
        server: Server,
        client: DiscordClientImpl,
        json: JsonObject
) : StoreChannel, ServerChannelImpl(server, client, json["id"].asLong) {
    @Volatile
    override var nsfw = false
    @Volatile
    override var category: ChannelCategory? = null

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        nsfw = json.getOrNull("nsfw")?.asBoolean == true
        position = json["position"].asInt

        json.getOrNull("parent_id")?.asLongOrNull?.let {
            category = server.channelCategories.find(it)
        }

        loadPermissionOverwrites(json)
    }

    private fun checkExistence() {
        if (server.storeChannels.find(id) == null) {
            throw NotFoundException()
        }
    }
}