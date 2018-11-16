package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventType = "GUILD_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data.getOrNull("unavailable")?.boolean == true) {
            return
        }

        val id = data["id"].long
        val isLarge = data["large"].booleanOrNull == true

        // Update server after reconnection
        client.servers.find(id)?.let {
            it as ServerImpl
            it.update(data)

            if (isLarge) {
                client.gateway.memberChunkRequestQueue.offer(id)
            } else {
                it.ready = true

                if (!it.initialized.getAndSet(true)) {
                    client.eventManager.fire(ServerReadyEvent(it))
                }
            }
            return
        }

        val server = client.servers.updateOrPut(id, data) {
            ServerImpl(client, data["id"].long).apply { update(data) }
        } as ServerImpl

        if (isLarge) {
            // Request additional members
            client.gateway.memberChunkRequestQueue.offer(id)
        } else {
            server.ready = true

            if (!server.initialized.getAndSet(true)) {
                client.eventManager.fire(ServerReadyEvent(server))
            }
        }
    }
}