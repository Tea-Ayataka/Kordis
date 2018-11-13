package net.ayataka.kordis

import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySet
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.user.User

interface DiscordClient {
    val status: ConnectionStatus
    val botUser: User

    val servers: NameableEntitySet<Server>
    val users: EntitySet<User>
    val privateChannels: EntitySet<PrivateTextChannel>

    suspend fun addListener(listener: Any)
    suspend fun removeListener(listener: Any)

    /**
     * Update the bot's activity
     */
    fun updateStatus(status: UserStatus, type: ActivityType, name: String)

    /**
     * Get a user by its id
     */
    suspend fun getUser(id: Long): User?

    suspend fun getServer(id: Long): Server?

    /**
     * Get a user by query
     */
    suspend fun findUsers(query: String): List<User>

    suspend fun findServers(query: String): List<Server>
}