package net.ayataka.kordis.websocket.handlers.other

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.ServerShutdownEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ReadyHandler : GatewayHandler {
    override val eventType = "READY"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        // Set the bot user
        client.botUser = client.users.getOrPut(data["user"].jsonObject["id"].long) { UserImpl(client, data["user"].jsonObject) }

        // Initialize servers
        val serverIds = data["guilds"].jsonArray.map { it.jsonObject["id"].long }

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