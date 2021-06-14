package net.ayataka.kordis.websocket.handlers.channel

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.channel.PrivateTextChannelImpl
import net.ayataka.kordis.event.events.server.channel.ChannelUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelUpdateHandler : GatewayHandler {
    override val eventType = "CHANNEL_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (!data.has("guild_id")) {
            client.privateChannels.updateOrPut(data["id"].asLong, data) { PrivateTextChannelImpl(client, data) }
            return
        }

        val server = deserializeServer(client, data) ?: return
        if (!server.ready) {
            server.handleLater(eventType, data)
            return
        }

        val channel = updateServerChannel(server, data) ?: return
        client.eventManager.fire(ChannelUpdateEvent(channel))
    }
}