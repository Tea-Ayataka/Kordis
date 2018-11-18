package net.ayataka.kordis.websocket.handlers.message


import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.event.events.message.MessageReceiveEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class MessageCreateHandler : GatewayHandler {
    override val eventType = "MESSAGE_CREATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        client.eventManager.fire(MessageReceiveEvent(MessageImpl(client, data)))
    }
}