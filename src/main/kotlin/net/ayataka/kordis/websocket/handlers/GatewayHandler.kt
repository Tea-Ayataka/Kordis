package net.ayataka.kordis.websocket.handlers

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClient

interface GatewayHandler {
    fun handle(client: DiscordClient, data: JsonObject)
}