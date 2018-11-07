package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberRemoveHandler : GatewayHandler {
    override val eventName = "GUILD_MEMBER_REMOVE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        server.members.remove(data["user"].jsonObject["id"].long)
    }
}