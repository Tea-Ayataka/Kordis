package net.ayataka.kordis.websocket.handlers.message

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.message.MessageDeleteEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageDeleteBulkHandler : GatewayHandler {
    override val eventType = "MESSAGE_DELETE_BULK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val messageIds = data["ids"].asJsonArray.map { it.asLong }

        if (!data.has("guild_id")) {
            val channel = client.privateChannels.find(data["channel_id"].asLong) ?: return
            client.eventManager.fire(MessageDeleteEvent(messageIds, channel, null))
            return
        }

        val server = client.servers.find(data["guild_id"].asLong)
            ?: throw IllegalStateException("unknown server id received")

        val channelId = data["channel_id"].asLong
        val channel = server.textChannels.find(channelId)
            ?: server.announcementChannels.find(channelId)
            ?: throw IllegalStateException("unknown channel id received")

        client.eventManager.fire(MessageDeleteEvent(messageIds, channel, server))
    }
}