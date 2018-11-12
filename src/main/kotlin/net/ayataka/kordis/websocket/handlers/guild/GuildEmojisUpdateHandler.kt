package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.emoji.EmojiUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildEmojisUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_EMOJIS_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return
        server.updateEmojis(data)

        client.eventManager.fire(EmojiUpdateEvent(server))
    }
}