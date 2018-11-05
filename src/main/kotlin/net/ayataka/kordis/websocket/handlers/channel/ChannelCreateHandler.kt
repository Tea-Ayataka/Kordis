package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelCreateHandler : GatewayHandler {
    override val eventName = "CHANNEL_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}