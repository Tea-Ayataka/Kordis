package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleUpdateHandler : GatewayHandler {
    override val eventName = "GUILD_ROLE_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}