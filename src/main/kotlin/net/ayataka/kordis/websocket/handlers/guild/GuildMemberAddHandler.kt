package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberAddHandler : GatewayHandler {
    override val eventName = "GUILD_MEMBER_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val user = client.users.getOrPut(data["user"].long) { UserImpl(client, data["user"].jsonObject) }

        server.members.put(MemberImpl(client, data, server, user))
    }
}