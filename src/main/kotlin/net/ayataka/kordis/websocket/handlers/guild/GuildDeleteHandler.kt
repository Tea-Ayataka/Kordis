package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildDeleteHandler : GatewayHandler {
    override val eventName = "GUILD_DELETE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        client.servers.remove(data["id"].long)
    }
}