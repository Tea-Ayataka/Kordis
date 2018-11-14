package net.ayataka.kordis.websocket.handlers.voice

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class VoiceServerUpdateHandler : GatewayHandler {
    override val eventType = "VOICE_SERVER_UPDATE"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}