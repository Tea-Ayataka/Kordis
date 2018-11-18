package net.ayataka.kordis.websocket.handlers


import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl

interface GatewayHandler {
    val eventType: String
    fun handle(client: DiscordClientImpl, data: JsonObject)
}