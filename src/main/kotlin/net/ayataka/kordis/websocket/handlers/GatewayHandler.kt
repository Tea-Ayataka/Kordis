package net.ayataka.kordis.websocket.handlers

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl

interface GatewayHandler {
    val eventType: String
    fun handle(client: DiscordClientImpl, data: JsonObject)

    fun deserializeServer(client: DiscordClientImpl, data: JsonObject, postponeIfNotExist: Boolean = true): ServerImpl? {
        val server = client.servers.find(data["guild_id"].asLong) as? ServerImpl
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
}