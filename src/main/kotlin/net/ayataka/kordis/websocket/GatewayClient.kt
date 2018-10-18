package net.ayataka.kordis.websocket

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import net.ayataka.kordis.DiscordClient

class GatewayClient(private val discordClient: DiscordClient) {
    val client = HttpClient { install(WebSockets) }

    fun connect(endpoint: String) {
        GlobalScope.launch(CoroutineName("WebSocket Handler")) {
            client.webSocket(request = { url(endpoint) }) {
                incoming.consumeEach {
                    if (it is Frame.Text) {
                        println(it.readText())
                    }
                }
            }
        }
    }
}