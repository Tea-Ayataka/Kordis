package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.role.RoleImpl
import net.ayataka.kordis.event.events.server.role.RoleCreateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleCreateHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data) ?: return
        val roleObject = data["role"].asJsonObject
        val role = server.roles.updateOrPut(roleObject["id"].asLong, roleObject) { RoleImpl(server, client, roleObject) }

        client.eventManager.fire(RoleCreateEvent(role))
    }
}