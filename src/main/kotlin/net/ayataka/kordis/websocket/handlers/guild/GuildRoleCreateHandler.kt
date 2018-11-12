package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.role.RoleImpl
import net.ayataka.kordis.event.events.server.role.RoleCreateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleCreateHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val role = server.roles.updateOrPut(data["id"].long, data) { RoleImpl(server, client, data) }

        client.eventManager.fire(RoleCreateEvent(role))
    }
}