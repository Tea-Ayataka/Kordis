package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerShutdownEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildDeleteHandler : GatewayHandler {
    override val eventType = "GUILD_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["id"].long) as? ServerImpl ?: return
        val unavailable = data.getOrNull("unavailable")?.boolean ?: false
        
        if (unavailable) {
            server.ready = false
        } else {
            client.servers.remove(server.id)
            client.eventManager.fire(ServerShutdownEvent(server))
        }
    }
}