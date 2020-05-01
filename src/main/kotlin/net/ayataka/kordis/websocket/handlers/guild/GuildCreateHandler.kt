package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventType = "GUILD_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data.getOrNull("unavailable")?.asBoolean == true) {
            return
        }

        val id = data["id"].asLong

        val server = client.servers.updateOrPut(id, data) {
            ServerImpl(client, data["id"].asLong).apply { update(data) }
        } as ServerImpl

        client.gateway.handlePostponedServerEvents(server)

        server.ready = true
        if (!server.initialized.getAndSet(true)) {
            LOGGER.trace("Server ready: ${server.name} (${server.id})")
            client.eventManager.fire(ServerReadyEvent(server))
        }
    }
}