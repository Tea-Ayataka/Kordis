package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildRoleDeleteHandler : GatewayHandler {
    override val eventName = "GUILD_ROLE_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}