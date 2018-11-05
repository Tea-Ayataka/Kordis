package net.ayataka.kordis.test

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.event.EventListener
import net.ayataka.kordis.event.events.ServerReadyEvent

fun main(args: Array<String>) = runBlocking {
    TestBot().start(args[0])
}

class TestBot {
    suspend fun start(token: String) {
        val client = Kordis.create {
            this.token = token
        }

        client.addListener(this)
        client.connect()

        client.servers.findByName("TestServer")
        client.servers.find(9999)
        client.servers.forEach {
            println(it.name)
        }

        client.users.find(-1)

        delay(99999999999999)
    }

    @EventListener
    suspend fun onServerReady(event: ServerReadyEvent) {
        println("Server Ready! ${event.server.name}")
        println("Members:")
        event.server.members.find(9999999)
        event.server.members.forEach {
            println("Name: ${it.tag}, Roles: ${it.roles.joinToString()}}")
        }
        println("Text Channels:")
        event.server.textChannels.forEach {
            println(it)
        }

        val channel = event.server.textChannels.findByName("test")!!
        val secondChannel = event.server.textChannels.findByName("t")!!
    }
}