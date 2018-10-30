package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildEmojisUpdateHandler : GatewayHandler {
    override val eventName = "GUILD_EMOJIS_UPDATE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}