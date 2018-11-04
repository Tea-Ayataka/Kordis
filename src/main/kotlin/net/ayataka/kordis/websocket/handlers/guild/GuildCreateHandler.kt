package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.ServerReadyEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildCreateHandler : GatewayHandler {
    override val eventName = "GUILD_CREATE"

    override fun handle(client: DiscordClient, data: JsonObject) {
        if (data["unavailable"].boolean) {
            return
        }

        val server = ServerImpl(client, data)

        if (data["large"].booleanOrNull == true) {
            // Request additional members
            client.gateway.memberChunkRequestQueue.offer(data["id"].long)
        } else {
            client.eventManager.fire(ServerReadyEvent(server))
        }
    }
}