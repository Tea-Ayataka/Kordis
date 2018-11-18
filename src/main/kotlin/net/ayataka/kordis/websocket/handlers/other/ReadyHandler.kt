package net.ayataka.kordis.websocket.handlers.other

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.ServerShutdownEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ReadyHandler : GatewayHandler {
    override val eventType = "READY"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        // Set the bot user
        client.botUser = client.users.getOrPut(data["user"].asJsonObject["id"].asLong) { UserImpl(client, data["user"].asJsonObject) }

        // Initialize servers
        val serverIds = data["guilds"].asJsonArray.map { it.asJsonObject["id"].asLong }

        serverIds.forEach {
            client.servers.getOrPut(it) { ServerImpl(client, it) }
        }

        // Shutdown invalid servers (after reconnection)
        client.servers.removeAll {
            if (it.id !in serverIds) {
                client.eventManager.fire(ServerShutdownEvent(it))
                true
            } else {
                false
            }
        }
    }
}