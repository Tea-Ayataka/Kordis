package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.server.ServerUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.update(data["id"].asLong, data)
        if (server == null) {
            client.gateway.postponeServerEvent(eventType, data, data["id"].asLong)
            return
        }
        client.eventManager.fire(ServerUpdateEvent(server))
    }
}