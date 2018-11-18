package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class PresenseUpdateHandler : GatewayHandler {
    override val eventType = "PRESENCE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl ?: return

        // Update user
        val userObject = data["user"].asJsonObject
        val user = client.users.find(userObject["id"].asLong) ?: return

        // Update member presence
        val member = server.members.find(user.id) as? MemberImpl ?: return

        member.updatePresence(data)
    }
}