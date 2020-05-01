package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.role.RoleUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data) ?: return

        // Update role
        val role = server.roles.update(data["role"].asJsonObject["id"].asLong, data["role"].asJsonObject)

        if (role == null) {
            if (!server.ready) {
                server.handleLater(eventType, data)
            }
            return
        }

        client.eventManager.fire(RoleUpdateEvent(role))
    }
}