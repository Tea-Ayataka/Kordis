package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelDeleteHandler : GatewayHandler {
    override val eventName = "CHANNEL_DELETE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}