package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.RoleImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleCreateHandler : GatewayHandler {
    override val eventName = "GUILD_ROLE_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return

        server.roles.add(RoleImpl(client, data, server))
    }
}