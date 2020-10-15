package net.ayataka.kordis

import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySetImpl
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.websocket.GatewayClient

@Suppress("OVERRIDE_BY_INLINE")
class DiscordClientImpl(
        val token: String,
        val shard: Int,
        val maxShards: Int,
        val intents: Set<GatewayIntent>
) : DiscordClient {
    override var status = ConnectionStatus.DISCONNECTED
    override lateinit var botUser: User

    val rest = RestClient(this)
    lateinit var gateway: GatewayClient
        private set

    val eventManager = EventManager()

    override val servers = NameableEntitySetImpl<Server>()
    override val users = NameableEntitySetImpl<User>()
    override val privateChannels = EntitySetImpl<PrivateTextChannel>()

    override suspend fun connect() {
        if (intents.isNotEmpty() && GatewayIntent.GUILD_MEMBERS !in intents) {
            throw IllegalArgumentException("You need to include GUILD_MEMBERS intent when the specified intents is not empty.")
        }

        if (status != ConnectionStatus.DISCONNECTED) {
            throw IllegalStateException()
        }

        status = ConnectionStatus.CONNECTING

        // Connect to the gateway
        val endpoint = rest.request(Endpoint.GET_GATEWAY_BOT.format()).asJsonObject["url"].asString
        gateway = GatewayClient(this, endpoint, intents)
        gateway.connect()

        status = ConnectionStatus.CONNECTED
    }

    override fun addListener(listener: Any, serverId: Long?) {
        eventManager.register(listener, serverId)
    }

    override fun removeListener(listener: Any, serverId: Long?) {
        eventManager.unregister(listener, serverId)
    }

    override fun updateStatus(status: UserStatus, type: ActivityType, name: String) {
        gateway.updateStatus(status, type, name)
    }

    override suspend fun getUser(id: Long): User? {
        users.find(id)?.let { return it }

        return try {
            rest.request(Endpoint.GET_USER.format("user.id" to id))
                    .let { users.updateOrPut(id, it.asJsonObject) { UserImpl(this, it.asJsonObject) } }
        } catch (ex: NotFoundException) {
            return null
        }
    }
}