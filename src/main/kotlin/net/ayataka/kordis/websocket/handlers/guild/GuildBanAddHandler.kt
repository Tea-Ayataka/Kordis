package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanAddHandler : GatewayHandler {
    override val eventName = "GUILD_BAN_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}