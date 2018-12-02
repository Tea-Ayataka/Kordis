package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.everyone
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.event.events.server.user.UserRoleUpdateEvent
import net.ayataka.kordis.event.events.server.user.UserUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBER_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl ?: return
        val userId = data["user"].asJsonObject["id"].asLong

        val member = server.members.find(userId) as? MemberImpl

        if (member == null) {
            if (!server.ready) {
                server.handleLater(eventType, data)
            }
            return
        }

        val roleIdsBefore = member.roles.map { it.id }
        val roleIdsAfter = data["roles"].asJsonArray.map { it.asLong }.plus(server.roles.everyone.id)

        member.update(data)
        client.eventManager.fire(UserUpdateEvent(member))

        if (roleIdsBefore.size != roleIdsAfter.size || !roleIdsBefore.containsAll(roleIdsAfter)) {
            client.eventManager.fire(UserRoleUpdateEvent(member, roleIdsBefore.mapNotNull { server.roles.find(it) }))
        }
    }
}