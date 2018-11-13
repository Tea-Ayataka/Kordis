package net.ayataka.kordis.websocket.handlers.message

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.event.events.message.MessageEditEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageUpdateHandler : GatewayHandler {
    override val eventType = "MESSAGE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data.containsKey("content")) {
            client.eventManager.fire(MessageEditEvent(MessageImpl(client, data)))
            return
        }

        val channel = data.getOrNull("guild_id")
                ?.let { client.servers.find(it.long)?.textChannels?.find(data["channel_id"].long) }
                ?: client.privateChannels.find(data["channel_id"].long)
                ?: return

        GlobalScope.launch {
            channel.getMessage(data["id"].long)?.let {
                client.eventManager.fire(MessageEditEvent(it))
            }
        }
    }
}