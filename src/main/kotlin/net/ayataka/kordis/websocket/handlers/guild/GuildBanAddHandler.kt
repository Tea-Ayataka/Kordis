package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.user.UserBanEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanAddHandler : GatewayHandler {
    override val eventType = "GUILD_BAN_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl ?: return
        val userObject = data["user"].asJsonObject
        val user = client.users.updateOrPut(userObject["id"].asLong, userObject) { UserImpl(client, userObject) }

        client.eventManager.fire(UserBanEvent(server, user))
    }
}