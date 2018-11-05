package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMembersChunkHandler : GatewayHandler {
    override val eventName = "GUILD_MEMBERS_CHUNK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}