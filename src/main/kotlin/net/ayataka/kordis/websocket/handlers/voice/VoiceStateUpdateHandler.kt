package net.ayataka.kordis.websocket.handlers.voice

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class VoiceStateUpdateHandler : GatewayHandler {
    override val eventType = "VOICE_STATE_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {

    }
}