package net.ayataka.kordis

import kotlinx.serialization.json.content
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySetImpl
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.EventManager
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.rest.RestClient
import net.ayataka.kordis.utils.DISCORD_ID
import net.ayataka.kordis.utils.FULL_USER_REF
import net.ayataka.kordis.utils.USER_MENTION
import net.ayataka.kordis.websocket.GatewayClient

@Suppress("OVERRIDE_BY_INLINE")
class DiscordClientImpl(
        val token: String,
        val shard: Int,
        val maxShards: Int
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

    suspend fun connect() {
        if (status != ConnectionStatus.DISCONNECTED) {
            throw IllegalStateException()
        }

        status = ConnectionStatus.CONNECTING

        // Connect to the gateway
        val endpoint = rest.request(Endpoint.GET_GATEWAY_BOT.format()).jsonObject["url"].content
        gateway = GatewayClient(this, endpoint)
        gateway.connectBlocking()

        status = ConnectionStatus.CONNECTED
    }

    override suspend fun addListener(listener: Any) {
        eventManager.register(listener)
    }

    override suspend fun removeListener(listener: Any) {
        eventManager.unregister(listener)
    }

    override fun updateStatus(status: UserStatus, type: ActivityType, name: String) {
        gateway.updateStatus(status, type, name)
    }

    override suspend fun getUser(id: Long): User? {
        users.find(id)?.let { return it }

        return try {
            rest.request(Endpoint.GET_USER.format("user.id" to id))
                    .let { users.updateOrPut(id, it.jsonObject) { UserImpl(this, it.jsonObject) } }
        } catch (ex: NotFoundException) {
            return null
        }
    }

    override suspend fun getServer(id: Long): Server? {
        servers.find(id)?.let { return it }

        return try {
            rest.request(Endpoint.GET_GUILD.format("guild.id" to id))
                    .let { servers.updateOrPut(id, it.jsonObject) { ServerImpl(this, id) } }
        } catch (ex: NotFoundException) {
            return null
        }
    }

    override suspend fun findUsers(query: String): List<User> {
        val userMention = USER_MENTION.matcher(query)
        val fullRefMatch = FULL_USER_REF.matcher(query)

        if (userMention.matches()) {
            val user = this.getUser(userMention.group(1).toLong())

            if (user != null)
                return listOf(user)
        } else if (fullRefMatch.matches()) {
            val lower = fullRefMatch.group(1).toLowerCase()
            val discriminator = fullRefMatch.group(2)
            val users = this.users
                    .filter { it.name.toLowerCase() == lower && it.discriminator == discriminator }

            if (users.isNotEmpty())
                return users
        } else if (DISCORD_ID.matcher(query).matches()) {
            val user = this.getUser(query.toLong())
            if (user != null)
                return listOf(user)
        }

        return this.users.findByQuery(query)
    }

    override suspend fun findServers(query: String): List<Server> {
        if (DISCORD_ID.matcher(query).matches()) {
            val server = this.getServer(query.toLong())
            if (server != null)
                return listOf(server)
        }

        return this.servers.findByQuery(query)
    }
}