package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelCreateHandler : GatewayHandler {
    override val eventType = "CHANNEL_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = data.getOrNull("guild_id")?.let { client.servers.find(it.long) } as? ServerImpl
        val id = data["id"].long

        when (data["type"].int) {
            ChannelType.GUILD_TEXT.id -> {
                server?.textChannels?.updateOrPut(id, data) { ServerTextChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_VOICE.id -> {
                server?.voiceChannels?.updateOrPut(id, data) { ServerVoiceChannelImpl(server, client, data) }
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server?.channelCategories?.updateOrPut(id, data) { ChannelCategoryImpl(server, client, data) }
            }
            else -> {
                LOGGER.error("Invalid channel type received: ${data["type"].int}")
            }
        }
    }
}