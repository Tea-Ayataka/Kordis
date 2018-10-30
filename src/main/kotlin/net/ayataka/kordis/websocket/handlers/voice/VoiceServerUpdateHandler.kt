package net.ayataka.kordis.websocket.handlers.voice

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class VoiceServerUpdateHandler : GatewayHandler {
    override val eventName = "VOICE_SERVER_UPDATE"

    override fun handle(client: DiscordClient, data: JsonObject) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}