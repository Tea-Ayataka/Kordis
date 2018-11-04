package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.event.EventListener
import net.ayataka.kordis.event.events.ServerReadyEvent

fun main(args: Array<String>) = runBlocking {
    TestBot().start(args[0])
}

class TestBot {
    suspend fun start(token: String) {
        val client = DiscordClient(token)
        client.eventManager.register(this)
        client.connect()
        delay(99999999999999)
    }

    @EventListener
    fun onServerReady(event: ServerReadyEvent) {
        println("Server Ready! ${event.server.name}")
        println("Members:")
        event.server.members.forEach {
            println("Name: ${it.tag}, Roles: ${it.roles.joinToString() }}")
        }
    }
}