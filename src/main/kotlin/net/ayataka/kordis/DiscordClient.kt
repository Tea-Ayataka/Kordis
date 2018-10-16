package net.ayataka.kordis

import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient

class DiscordClient {
    var status = ConnectionStatus.DISCONNECTED
        private set

    private val gateway = GatewayClient(this)
    private val rest = RestClient(this)

    /**
     * EventManager
     */
    val eventManager = EventManager()

    suspend fun connect(token: String, shard: Int = 0, maxShards: Int = 0) {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw UnsupportedOperationException("reusing this instance is not supported")
        }

    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}