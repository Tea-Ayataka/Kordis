package net.ayataka.kordis.websocket.handlers.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.message.MessageDeleteEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageDeleteBulkHandler : GatewayHandler {
    override val eventType = "MESSAGE_DELETE_BULK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val messageIds = data["ids"].jsonArray.map { it.long }

        if (!data.containsKey("guild_id")) {
            val channel = client.privateChannels.find(data["channel_id"].long) ?: return
            client.eventManager.fire(MessageDeleteEvent(messageIds, channel, null))
            return
        }

        val server = client.servers.find(data["guild_id"].long)
                ?: throw IllegalStateException("unknown server id received")

        val channel = server.textChannels.find(data["channel_id"].long)
                ?: throw IllegalStateException("unknown channel id received")

        client.eventManager.fire(MessageDeleteEvent(messageIds, channel, server))
    }
}