package net.ayataka.kordis.websocket.handlers.voice

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class VoiceStateUpdateHandler : GatewayHandler {
    override val eventName = "VOICE_STATE_UPDATE"

    override fun handle(client: DiscordClient, data: JsonObject) {

    }
}