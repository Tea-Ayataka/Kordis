package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelUpdateHandler : GatewayHandler {
    override val eventType = "CHANNEL_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = data.getOrNull("guild_id")?.let { client.servers.find(it.long) } as? ServerImpl
        val id = data["id"].long

        when (data["type"].int) {
            ChannelType.GUILD_TEXT.id -> {
                server?.textChannels?.update(id, data)
            }
            ChannelType.GUILD_VOICE.id -> {
                server?.voiceChannels?.update(id, data)
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server?.channelCategories?.update(id, data)
            }
            else -> {
                LOGGER.error("Invalid channel type received: ${data["type"].int}")
            }
        }
    }
}