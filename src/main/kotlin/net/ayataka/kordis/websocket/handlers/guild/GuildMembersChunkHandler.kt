package net.ayataka.kordis.websocket.handlers.guild

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMembersChunkHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBERS_CHUNK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val nonce = data["nonce"].asString
        val server = deserializeServer(client, data, false)
        if (server == null) {
            client.gateway.onReceivedMemberChunk(nonce, emptyList())
            return
        }

        val chunk = data["members"].asJsonArray.mapNotNull {
            deserializeMember(client, it.asJsonObject, server)
        }

        client.gateway.onReceivedMemberChunk(nonce, chunk)
    }
}