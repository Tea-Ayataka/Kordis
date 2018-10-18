package net.ayataka.kordis

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient

class DiscordClient {
    var status = ConnectionStatus.DISCONNECTED
        private set

    lateinit var token: String
    private val gateway = GatewayClient(this)
    private val rest = RestClient(this)

    val eventManager = EventManager()

    suspend fun connect(token: String, shard: Int = 0, maxShards: Int = 0) {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw UnsupportedOperationException("reusing this instance is not supported")
        }

        this.token = token
        gateway.connect("wss://gateway.discord.gg/")
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}

fun main(args: Array<String>) {
    runBlocking {
        DiscordClient().connect("Unknown")
        delay(99999999999999)
    }
}