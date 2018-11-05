package net.ayataka.kordis

import kotlinx.serialization.json.content
import net.ayataka.kordis.entity.collection.ServerListImpl
import net.ayataka.kordis.entity.collection.UserListImpl
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient
import org.apache.logging.log4j.LogManager

val LOGGER = LogManager.getLogger()

class DiscordClientImpl(val token: String, val shard: Int = 0, val maxShards: Int = 0) : DiscordClient {
    override var status = ConnectionStatus.DISCONNECTED

    val rest = RestClient(this)
    lateinit var gateway: GatewayClient
        private set

    val eventManager = EventManager()

    override val servers = ServerListImpl()
    override val users = UserListImpl()

    override suspend fun connect() {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw UnsupportedOperationException("")
        }

        status = ConnectionStatus.CONNECTING

        // Connect to the gateway
        val gatewayEndpoint = rest.request(Endpoint.GET_GATEWAY_BOT.format())["url"].content
        gateway = GatewayClient(this, gatewayEndpoint)
        gateway.connectBlocking()

        status = ConnectionStatus.CONNECTED
    }

    override suspend fun addListener(listener: Any) {
        eventManager.register(listener)
    }

    override suspend fun removeListener(listener: Any) {
        eventManager.unregister(listener)
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}