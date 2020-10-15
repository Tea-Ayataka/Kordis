package net.ayataka.kordis

import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySet
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.user.User

interface DiscordClient {
    /**
     * The connection status to the gateway
     */
    val status: ConnectionStatus

    /**
     * The user object of the bot
     */
    val botUser: User

    /**
     * The servers the bot is on
     */
    val servers: NameableEntitySet<Server>

    /**
     * The cache of discord users
     */
    val users: EntitySet<User>

    /**
     * The cache of private channels
     */
    val privateChannels: EntitySet<PrivateTextChannel>

    fun addListener(listener: Any, serverId: Long? = null)
    fun removeListener(listener: Any, serverId: Long? = null)

    /**
     * Connect to the gateway
     */
    suspend fun connect()

    /**
     * Update the bot's activity
     */
    fun updateStatus(status: UserStatus, type: ActivityType, name: String)

    /**
     * Get a user by its id
     */
    suspend fun getUser(id: Long): User?
}