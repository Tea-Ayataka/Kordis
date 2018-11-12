package net.ayataka.kordis

import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySet
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User

interface DiscordClient {
    val status: ConnectionStatus
    val botUser: User

    val servers: NameableEntitySet<Server>
    val users: EntitySet<User>
    val privateChannels: EntitySet<PrivateTextChannel>

    suspend fun addListener(listener: Any)
    suspend fun removeListener(listener: Any)
}