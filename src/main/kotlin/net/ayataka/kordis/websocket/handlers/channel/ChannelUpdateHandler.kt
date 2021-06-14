package net.ayataka.kordis.websocket.handlers.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.channel.announcement.AnnouncementChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.store.StoreChannelImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.event.events.server.channel.ChannelUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelUpdateHandler : GatewayHandler {
    override val eventType = "CHANNEL_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data)
        val id = data["id"].asLong

        val channel = when (data["type"].asInt) {
            ChannelType.GUILD_TEXT.id -> {
                server?.textChannels?.updateOrPut(id, data) { ServerTextChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_VOICE.id -> {
                server?.voiceChannels?.updateOrPut(id, data) { ServerVoiceChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server?.channelCategories?.updateOrPut(id, data) { ChannelCategoryImpl(server, client, data) }
            }
            ChannelType.GUILD_NEWS.id -> {
                server?.announcementChannels?.updateOrPut(id, data) { AnnouncementChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_STORE.id -> {
                server?.storeChannels?.updateOrPut(id, data) { StoreChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_STAGE_VOICE.id -> {
                server?.voiceChannels?.updateOrPut(id, data) { ServerVoiceChannelImpl(server, client, data) }
            }
            else -> {
                throw IllegalStateException("unknown channel type received")
            }
        }

        channel?.let { client.eventManager.fire(ChannelUpdateEvent(it)) }
    }
}