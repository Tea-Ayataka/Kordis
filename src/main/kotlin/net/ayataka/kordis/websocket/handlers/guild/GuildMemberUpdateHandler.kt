package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBER_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val userId = data["user"].jsonObject["id"].long

        server.members.update(userId, data)
    }
}