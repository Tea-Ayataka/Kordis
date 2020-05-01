package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.event.events.server.emoji.EmojiUpdateEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildEmojisUpdateHandler : GatewayHandler {
    override val eventType = "GUILD_EMOJIS_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = deserializeServer(client, data) ?: return
        server.updateEmojis(data)
        client.eventManager.fire(EmojiUpdateEvent(server))
    }
}