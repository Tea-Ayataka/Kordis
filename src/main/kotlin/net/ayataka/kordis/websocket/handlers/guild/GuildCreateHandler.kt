package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.ServerReadyEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventName = "GUILD_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data["unavailable"].boolean) {
            return
        }

        val id = data["id"].long
        val isLarge = data["large"].booleanOrNull == true

        // Update server when reconnect
        client.servers.find(id)?.let {
            (it as ServerImpl).update(data)

            if (isLarge) {
                client.gateway.memberChunkRequestQueue.offer(id)
            }
            return
        }

        val server = client.servers.add(id) { ServerImpl(client, data) }
        if (isLarge) {
            // Request additional members
            client.gateway.memberChunkRequestQueue.offer(id)
        } else {
            client.eventManager.fire(ServerReadyEvent(server))
        }
    }
}