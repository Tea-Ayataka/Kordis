package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.timer
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventType = "GUILD_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data.getOrNull("unavailable")?.asBoolean == true) {
            return
        }

        val id = data["id"].asLong
        val isLarge = data.getOrNull("large")?.asBoolean == true

        // Update server after reconnection
        client.servers.find(id)?.let {
            it as ServerImpl
            it.update(data)

            if (!isLarge) {
                it.ready()
                if (!it.initialized.getAndSet(true)) {
                    client.eventManager.fire(ServerReadyEvent(it))
                }
            } else {
                prepare(client, it)
            }
        }

        val server = client.servers.updateOrPut(id, data) {
            ServerImpl(client, data["id"].asLong).apply { update(data) }
        } as ServerImpl

        if (!isLarge) {
            server.ready = true

            if (!server.initialized.getAndSet(true)) {
                client.eventManager.fire(ServerReadyEvent(server))
            }
        } else {
            prepare(client, server)
        }
    }

    private fun prepare(client: DiscordClientImpl, server: ServerImpl) {
        // Request additional members
        server.ready = false
        server.members.clear()
        server.removedMembers.clear()
        client.gateway.memberChunkRequestQueue.offer(server.id)

        GlobalScope.timer(1000, context = CoroutineName("Server Preparer")) {
            if (server.members.size >= server.memberCount.get() && !server.ready) {
                server.ready()

                if (!server.initialized.getAndSet(true)) {
                    client.eventManager.fire(ServerReadyEvent(server))
                }

                cancel()
            }
        }
    }
}