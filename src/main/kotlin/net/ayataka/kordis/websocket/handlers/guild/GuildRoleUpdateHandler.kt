package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleUpdateHandler : GatewayHandler {
    override val eventName = "GUILD_ROLE_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val roleData = data["role"].jsonObject

        server.roles.update(roleData["id"].long, roleData)
    }
}