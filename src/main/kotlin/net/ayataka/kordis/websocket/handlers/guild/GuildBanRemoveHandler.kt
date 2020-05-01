package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.server.user.UserUnbanEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanRemoveHandler : GatewayHandler {
    override val eventType = "GUILD_BAN_REMOVE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val user = deserializeUser(client, data)
        val server = deserializeServer(client, data) ?: return

        client.eventManager.fire(UserUnbanEvent(server, user))
    }
}