package net.ayataka.kordis

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.content
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient

class DiscordClientImpl(
        val token: String,
        val shard: Int,
        val maxShards: Int,
        listeners: Collection<Any>
) : DiscordClient {
    override var status = ConnectionStatus.DISCONNECTED
    override lateinit var botUser: User

    val rest = RestClient(this)
    lateinit var gateway: GatewayClient
        private set

    val eventManager = EventManager()

    override val servers = NameableEntitySetImpl<Server>()
    override val users = NameableEntitySetImpl<User>()

    init {
        runBlocking {
            listeners.forEach { eventManager.register(it) }
        }
    }

    suspend fun connect() {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw IllegalStateException()
        }

        status = ConnectionStatus.CONNECTING

        // Connect to the gateway
        val endpoint = rest.request(Endpoint.GET_GATEWAY_BOT.format()).jsonObject["url"].content
        gateway = GatewayClient(this, shard, maxShards, endpoint)
        gateway.connectBlocking()

        status = ConnectionStatus.CONNECTED

        object {
            fun aaa() {

            }
        }
    }

    override suspend fun addListener(listener: Any) {
        eventManager.register(listener)
    }

    override suspend fun removeListener(listener: Any) {
        eventManager.unregister(listener)
    }
}