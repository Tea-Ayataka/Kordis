package net.ayataka.kordis.websocket.handlers.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.event.events.server.channel.ChannelUpdateEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelUpdateHandler : GatewayHandler {
    override val eventType = "CHANNEL_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = data.getOrNull("guild_id")?.let { client.servers.find(it.asLong) } as? ServerImpl
        val id = data["id"].asLong

        val channel = when (data["type"].asInt) {
            ChannelType.GUILD_TEXT.id -> {
                server?.textChannels?.update(id, data)
            }
            ChannelType.GUILD_VOICE.id -> {
                server?.voiceChannels?.update(id, data)
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server?.channelCategories?.update(id, data)
            }
            ChannelType.GUILD_NEWS.id -> {
                server?.announcementChannels?.update(id, data)
            }
            ChannelType.GUILD_STORE.id -> {
                server?.storeChannels?.update(id, data)
            }
            else -> {
                throw IllegalStateException("unknown channel type received")
            }
        }

        channel?.let { client.eventManager.fire(ChannelUpdateEvent(it)) }
    }
}