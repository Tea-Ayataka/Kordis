package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.channel.PrivateTextChannelImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.event.events.server.channel.ChannelCreateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelCreateHandler : GatewayHandler {
    override val eventType = "CHANNEL_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val id = data["id"].long
        if (!data.containsKey("guild_id")) {
            client.privateChannels.updateOrPut(id, data) { PrivateTextChannelImpl(client, data) }
            return
        }

        val server = client.servers.find(data["guild_id"].long) as? ServerImpl
                ?: throw IllegalStateException("unknown server id received $data")

        val channel = when (data["type"].int) {
            ChannelType.GUILD_TEXT.id -> {
                server.textChannels.updateOrPut(id, data) { ServerTextChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_VOICE.id -> {
                server.voiceChannels.updateOrPut(id, data) { ServerVoiceChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server.channelCategories.updateOrPut(id, data) { ChannelCategoryImpl(server, client, data) }
            }
            else -> {
                throw IllegalStateException("unknown channel type received $data")
            }
        }

        client.eventManager.fire(ChannelCreateEvent(channel))
    }
}