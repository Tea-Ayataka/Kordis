package net.ayataka.kordis.websocket.handlers

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient

class GuildMemberRemoveHandler : GatewayHandler {
    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}