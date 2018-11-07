package net.ayataka.kordis.websocket.handlers.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.LOGGER
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.channel.CategoryImpl
import net.ayataka.kordis.entity.server.channel.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class ChannelCreateHandler : GatewayHandler {
    override val eventName = "CHANNEL_CREATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = data.getOrNull("guild_id")?.let { client.servers.find(it.long) } as? ServerImpl

        when (data["type"].int) {
            ChannelType.GUILD_TEXT.id -> {
                server?.textChannels?.add(ServerTextChannelImpl(server, client, data))
            }
            ChannelType.GUILD_VOICE.id -> {

            }
            ChannelType.GUILD_CATEGORY.id -> {
                server?.categories?.add(CategoryImpl(server, client, data))
            }
            else -> {
                LOGGER.error("Invalid channel type received: ${data["type"].int}")
            }
        }
    }
}