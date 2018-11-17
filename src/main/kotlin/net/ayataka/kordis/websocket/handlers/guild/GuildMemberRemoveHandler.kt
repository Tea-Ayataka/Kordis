package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.user.UserLeaveEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberRemoveHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBER_REMOVE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        val member = server.members.find(data["user"].jsonObject["id"].long) ?: return

        server.members.remove(member.id)
        server.memberCount.decrementAndGet()
        client.eventManager.fire(UserLeaveEvent(member))
    }
}