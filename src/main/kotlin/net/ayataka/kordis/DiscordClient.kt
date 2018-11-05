package net.ayataka.kordis

import net.ayataka.kordis.entity.collection.ServerList
import net.ayataka.kordis.entity.collection.UserList

interface DiscordClient {
    val status: ConnectionStatus

    val servers: ServerList
    val users: UserList

    suspend fun addListener(listener: Any)
    suspend fun removeListener(listener: Any)

    suspend fun connect()
}