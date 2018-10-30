package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanRemoveHandler : GatewayHandler {
    override val eventName = "GUILD_BAN_REMOVE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}