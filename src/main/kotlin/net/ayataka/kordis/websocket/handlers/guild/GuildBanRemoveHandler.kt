package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanRemoveHandler : GatewayHandler {
    override val eventType = "GUILD_BAN_REMOVE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}