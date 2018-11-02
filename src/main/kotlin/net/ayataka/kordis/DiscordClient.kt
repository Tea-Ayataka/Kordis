package net.ayataka.kordis

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.content
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap

val LOGGER = LogManager.getLogger()

class DiscordClient(val token: String, val shard: Int = 0, val maxShards: Int = 0) {
    var status = ConnectionStatus.DISCONNECTED

    private val rest = RestClient(this)
    lateinit var gateway: GatewayClient
        private set

    val eventManager = EventManager()
    val servers = mutableListOf<ServerImpl>()

    val userCache = ConcurrentHashMap<Long, User>()

    suspend fun connect() {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw UnsupportedOperationException("")
        }

        status = ConnectionStatus.CONNECTING

        // Connect to the gateway
        gateway = GatewayClient(this, rest.request(Endpoint.GET_GATEWAY_BOT.format())["url"].content)
        GlobalScope.launch { gateway.connectBlocking() }.join()

        status = ConnectionStatus.CONNECTED
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}