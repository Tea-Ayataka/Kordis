package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleCreateHandler : GatewayHandler {
    override val eventName = "GUILD_ROLE_CREATE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}