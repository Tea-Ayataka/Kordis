package net.ayataka.kordis.websocket.handlers

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClient

class MessageReactionRemoveHandler  : GatewayHandler{
    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}