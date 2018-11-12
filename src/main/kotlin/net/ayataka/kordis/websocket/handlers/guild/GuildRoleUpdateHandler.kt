package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.role.RoleUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_ROLE_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return

        // Update role
        val role = server.roles.update(data["role"].jsonObject["id"].long, data["role"].jsonObject) ?: return

        client.eventManager.fire(RoleUpdateEvent(role))
    }
}