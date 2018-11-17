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
        val userObject = data["user"].jsonObject
        val user = client.users.updateOrPut(userObject["id"].long, userObject) { UserImpl(client, userObject) }

        client.eventManager.fire(UserBanEvent(server, user))
    }
}