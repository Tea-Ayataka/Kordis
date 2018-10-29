package net.ayataka.kordis

import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient
import org.apache.logging.log4j.LogManager

val LOGGER = LogManager.getLogger()

class DiscordClient {
    var status = ConnectionStatus.DISCONNECTED
        private set

    lateinit var token: String
        private set

    lateinit var gateway: GatewayClient
        private set

    private val rest = RestClient(this)

    val eventManager = EventManager()

    suspend fun connect(token: String, shard: Int = 0, maxShards: Int = 0) {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw UnsupportedOperationException("reusing this instance is not supported")
        }

        this.token = token

        // Connect to the gateway
        gateway = GatewayClient(this, rest.request(Endpoint.GET_GATEWAY_BOT.format())["url"].asString)
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}