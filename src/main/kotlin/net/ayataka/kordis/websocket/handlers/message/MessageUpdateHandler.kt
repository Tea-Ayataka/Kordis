package net.ayataka.kordis.websocket.handlers.message

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.event.events.message.MessageEditEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageUpdateHandler : GatewayHandler {
    override val eventType = "MESSAGE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val channel = data.getOrNull("guild_id")
                ?.let { client.servers.find(it.asLong)?.textChannels?.find(data["channel_id"].asLong) }
                ?: client.privateChannels.find(data["channel_id"].asLong)
                ?: return

        if (data.has("edited_timestamp")) {
            val message = if (data.has("content")) MessageImpl(client, data) else null
            client.eventManager.fire(MessageEditEvent(channel, data["id"].asLong, message))
        }
    }
}