package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.user.UserJoinEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberAddHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBER_ADD"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl ?: return
        val user = client.users.getOrPut(data["user"].asJsonObject["id"].asLong) { UserImpl(client, data["user"].asJsonObject) }
        val member = server.members.updateOrPut(user.id, data) { MemberImpl(client, data, server, user) }

        server.memberCount.incrementAndGet()
        client.eventManager.fire(UserJoinEvent(member))
    }
}