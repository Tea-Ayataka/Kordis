package net.ayataka.kordis

import kotlinx.coroutines.experimental.sync.Mutex

class DiscordClient(val token: String) {
    private val mutex = Mutex()

    suspend fun connect() {

    }
}

enum class ConnectionStatus {
    READY, CONNECTING, CONNECTED
}