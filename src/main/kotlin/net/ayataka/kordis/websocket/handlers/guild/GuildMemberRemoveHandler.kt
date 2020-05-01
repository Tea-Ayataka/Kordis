package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.user.UserLeaveEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberRemoveHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBER_REMOVE"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val member = deserializeMember(client, data) ?: return
        val server = member.server as ServerImpl

        server.actualMemberCount.decrementAndGet()
        client.eventManager.fire(UserLeaveEvent(member))
    }
}