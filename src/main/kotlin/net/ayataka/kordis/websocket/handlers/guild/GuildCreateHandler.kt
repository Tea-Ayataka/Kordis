package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.timer
import net.ayataka.kordis.websocket.handlers.GatewayHandler
import java.util.concurrent.ConcurrentHashMap

class GuildCreateHandler : GatewayHandler {
    override val eventType = "GUILD_CREATE"

    private val preparers = ConcurrentHashMap<Long, Job>()

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        if (data.getOrNull("unavailable")?.asBoolean == true) {
            return
        }

        val id = data["id"].asLong
        val isLarge = data.getOrNull("large")?.asBoolean == true

        val server = client.servers.updateOrPut(id, data) {
            ServerImpl(client, data["id"].asLong).apply { update(data) }
        } as ServerImpl

        // Request additional members
        if (isLarge) {
            server.ready = false
            server.members.clear()
            client.gateway.requestMembers(server.id)

            if (preparers[server.id]?.isActive == true) {
                return
            }

            val time = System.currentTimeMillis()
            preparers[server.id] = GlobalScope.timer(1000, context = CoroutineName("Server Preparer")) {
                // Wait for 5 minutes maximum
                val timedOut = time <= System.currentTimeMillis() - 1000 * 60 * 5

                if (server.members.size >= server.memberCount.get() || timedOut) {
                    server.ready()

                    if (timedOut) {
                        LOGGER.warn("Timed out waiting for the all members of ${server.name} (${server.id})")
                    }

                    if (!server.initialized.getAndSet(true)) {
                        LOGGER.trace("Server ready: ${server.name} (${server.id})")
                        client.eventManager.fire(ServerReadyEvent(server))
                    }

                    cancel()
                    preparers.remove(server.id)
                }
            }
            return
        }

        server.ready = true
        if (!server.initialized.getAndSet(true)) {
            LOGGER.trace("Server ready: ${server.name} (${server.id})")
            client.eventManager.fire(ServerReadyEvent(server))
        }
    }
}