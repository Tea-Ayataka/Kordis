package net.ayataka.kordis

import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.collection.NameableIterableEntitySet
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User

interface DiscordClient {
    val status: ConnectionStatus

    val servers: NameableIterableEntitySet<Server>
    val users: NameableEntitySet<User>

    suspend fun addListener(listener: Any)
    suspend fun removeListener(listener: Any)

    suspend fun connect()
}