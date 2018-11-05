package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMemberUpdateHandler : GatewayHandler {
    override val eventName = "GUILD_MEMBER_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}