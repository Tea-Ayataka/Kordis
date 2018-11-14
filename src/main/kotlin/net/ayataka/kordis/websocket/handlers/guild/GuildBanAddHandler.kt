package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.user.UserBanEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanAddHandler : GatewayHandler {
    override val eventType = "GUILD_BAN_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val user = client.users.getOrPut(data["user"].long) { UserImpl(client, data["user"].jsonObject) }

        client.eventManager.fire(UserBanEvent(server, user))
    }
}