package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.role.RoleImpl
import net.ayataka.kordis.event.events.server.role.RoleUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data) ?: return

        // Update role
        val roleObject = data["role"].asJsonObject
        val role = server.roles.update(roleObject["id"].asLong, roleObject)

        // When failed to update
        if (role == null) {
            if (!server.ready) {
                server.handleLater(eventType, data)
            } else {
                val createdRole = server.roles.updateOrPut(roleObject["id"].asLong, roleObject) {
                    RoleImpl(server, client, roleObject)
                }
                client.eventManager.fire(RoleUpdateEvent(createdRole))
            }

            return
        }

        client.eventManager.fire(RoleUpdateEvent(role))
    }
}