package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.role.RoleDeleteEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleDeleteHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl ?: return
        val role = server.roles.find(data["role_id"].asLong) ?: return

        server.roles.remove(role.id)
        client.eventManager.fire(RoleDeleteEvent(role))
    }
}