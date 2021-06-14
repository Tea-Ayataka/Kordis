package net.ayataka.kordis.websocket.handlers.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.server.channel.ChannelDeleteEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelDeleteHandler : GatewayHandler {
    override val eventType = "CHANNEL_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val id = data["id"].asLong
        if (!data.has("guild_id")) {
            client.privateChannels.remove(id)
            return
        }

        val server = deserializeServer(client, data) ?: return
        if (!server.ready) {
            server.handleLater(eventType, data)
            return
        }

        val channel = server.textChannels.remove(id)
            ?: server.voiceChannels.remove(id)
            ?: server.channelCategories.remove(id)
            ?: server.announcementChannels.remove(id)
            ?: server.storeChannels.remove(id)
            ?: return

        client.eventManager.fire(ChannelDeleteEvent(channel))
    }
}