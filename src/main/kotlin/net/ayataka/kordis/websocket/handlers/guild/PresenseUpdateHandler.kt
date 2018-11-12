package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class PresenseUpdateHandler : GatewayHandler {
    override val eventType = "PRESENCE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return

        // Update user
        val userObject = data["user"].jsonObject
        val user = client.users.updateOrPut(userObject["id"].long, userObject) { UserImpl(client, userObject) }

        // Update member presence
        val member = server.members.find(user.id) as? MemberImpl ?: return
        member.updatePresence(data)
    }
}