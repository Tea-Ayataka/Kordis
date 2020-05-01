package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.event.events.server.ServerShutdownEvent
import net.ayataka.kordis.event.events.server.user.UserPresenceUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class PresenceUpdateHandler : GatewayHandler {
    override val eventType = "PRESENCE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val member = deserializeMember(client, data) ?: return
        client.eventManager.fire(UserPresenceUpdateEvent(member))
    }
}