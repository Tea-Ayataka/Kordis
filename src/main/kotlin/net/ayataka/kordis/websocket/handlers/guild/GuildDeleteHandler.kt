package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerShutdownEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildDeleteHandler : GatewayHandler {
    override val eventType = "GUILD_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data, false) ?: return
        val unavailable = data.getOrNull("unavailable")?.asBoolean ?: false

        if (unavailable) {
            server.ready = false
        } else {
            client.servers.remove(server.id)
            client.eventManager.fire(ServerShutdownEvent(server))
        }
    }
}