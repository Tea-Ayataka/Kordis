package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_UPDATE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        client.servers.update(data["id"].long, data)
    }
}