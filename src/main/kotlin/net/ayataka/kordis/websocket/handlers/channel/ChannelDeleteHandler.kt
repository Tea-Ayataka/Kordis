package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelDeleteHandler : GatewayHandler {
    override val eventName = "CHANNEL_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val id = data["id"].long

        if (server.textChannels.remove(id)) return
        if (server.voiceChannels.remove(id)) return
        if (server.channelCategories.remove(id)) return
    }
}