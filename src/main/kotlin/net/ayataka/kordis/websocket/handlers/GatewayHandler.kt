package net.ayataka.kordis.websocket.handlers


import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl

interface GatewayHandler {
    val eventType: String
    fun handle(client: DiscordClientImpl, data: JsonObject)
}