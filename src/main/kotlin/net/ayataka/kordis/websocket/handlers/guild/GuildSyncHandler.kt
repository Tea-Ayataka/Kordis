package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildSyncHandler : GatewayHandler {
    override val eventType = "GUILD_SYNC"
    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}