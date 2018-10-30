package net.ayataka.kordis.websocket.handlers


import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient

interface GatewayHandler {
    val eventName: String
    fun handle(client: DiscordClient, data: JsonObject)
}