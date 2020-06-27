package net.ayataka.kordis.websocket.handlers.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.channel.PrivateTextChannelImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.channel.announcement.AnnouncementChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.store.StoreChannelImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.event.events.server.channel.ChannelCreateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelCreateHandler : GatewayHandler {
    override val eventType = "CHANNEL_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val id = data["id"].asLong
        if (!data.has("guild_id")) {
            client.privateChannels.updateOrPut(id, data) { PrivateTextChannelImpl(client, data) }
            return
        }

        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl
                ?: throw IllegalStateException("unknown server id received")

        val channel = when (data["type"].asInt) {
            ChannelType.GUILD_TEXT.id -> {
                server.textChannels.updateOrPut(id, data) { ServerTextChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_VOICE.id -> {
                server.voiceChannels.updateOrPut(id, data) { ServerVoiceChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server.channelCategories.updateOrPut(id, data) { ChannelCategoryImpl(server, client, data) }
            }
            ChannelType.GUILD_NEWS.id -> {
                server.announcementChannels.updateOrPut(id, data) { AnnouncementChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_STORE.id -> {
                server.storeChannels.updateOrPut(id, data) { StoreChannelImpl(server, client, data) }
            }
            else -> {
                throw IllegalStateException("unknown channel type received")
            }
        }

        client.eventManager.fire(ChannelCreateEvent(channel))
    }
}