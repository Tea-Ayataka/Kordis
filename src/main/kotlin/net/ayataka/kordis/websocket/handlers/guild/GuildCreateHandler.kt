package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventName = "GUILD_CREATE"

    override fun handle(client: DiscordClient, data: JsonObject) {
        if (data["unavailable"].boolean) {
            return
        }

        // Queue
        client.gateway.memberChunkRequestQueue.offer(data["id"].long)
    }
}