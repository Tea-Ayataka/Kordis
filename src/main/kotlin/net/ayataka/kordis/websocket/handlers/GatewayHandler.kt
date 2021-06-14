package net.ayataka.kordis.websocket.handlers

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.announcement.AnnouncementChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.store.StoreChannelImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.enums.ChannelType
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl

interface GatewayHandler {
    val eventType: String
    fun handle(client: DiscordClientImpl, data: JsonObject)

    fun deserializeServer(client: DiscordClientImpl, data: JsonObject, postponeIfNotExist: Boolean = true): ServerImpl? {
        val serverId = if (data.has("guild_id")) data["guild_id"].asLong else data["id"].asLong
        val server = client.servers.find(serverId) as? ServerImpl
        if (server == null && postponeIfNotExist) {
            client.gateway.postponeServerEvent(eventType, data)
        }

        return server
    }

    fun deserializeUser(client: DiscordClientImpl, data: JsonObject): UserImpl {
        return client.users.updateOrPut(data["user"].asJsonObject["id"].asLong, data["user"].asJsonObject) {
            UserImpl(client, data["user"].asJsonObject)
        } as UserImpl
    }

    fun deserializeMember(client: DiscordClientImpl, data: JsonObject, server: ServerImpl? = null): MemberImpl? {
        val user = client.users.updateOrPut(data["user"].asJsonObject["id"].asLong, data["user"].asJsonObject) {
            UserImpl(client, data["user"].asJsonObject)
        }
        return MemberImpl(client, data, server ?: deserializeServer(client, data) ?: return null, user)
    }

    fun updateServerChannel(server: ServerImpl, data: JsonObject) : ServerChannel? {
        val id = data["id"].asLong

        return when (data["type"].asInt) {
            ChannelType.DM.id -> null
            ChannelType.GROUP_DM.id -> null
            ChannelType.GUILD_TEXT.id -> {
                server.textChannels.updateOrPut(id, data) { ServerTextChannelImpl(server, server.client, data) }
            }
            ChannelType.GUILD_VOICE.id -> {
                server.voiceChannels.updateOrPut(id, data) { ServerVoiceChannelImpl(server, server.client, data) }
            }
            ChannelType.GUILD_CATEGORY.id -> {
                server.channelCategories.updateOrPut(id, data) { ChannelCategoryImpl(server, server.client, data) }
            }
            ChannelType.GUILD_NEWS.id -> {
                server.announcementChannels.updateOrPut(id, data) {
                    AnnouncementChannelImpl(server, server.client, data)
                }
            }
            ChannelType.GUILD_STORE.id -> {
                server.storeChannels.updateOrPut(id, data) { StoreChannelImpl(server, server.client, data) }
            }
            ChannelType.GUILD_STAGE_VOICE.id -> {
                server.voiceChannels.updateOrPut(id, data) { ServerVoiceChannelImpl(server, server.client, data) }
            }
            else -> {
                throw IllegalStateException("unknown channel type received")
            }
        }
    }
}