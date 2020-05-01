package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.event.events.server.user.UserBanEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildBanAddHandler : GatewayHandler {
    override val eventType = "GUILD_BAN_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val user = deserializeUser(client, data)
        val server = deserializeServer(client, data) ?: return

        client.eventManager.fire(UserBanEvent(server, user))
    }
}