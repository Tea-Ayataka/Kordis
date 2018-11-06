package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.event.EventListener
import net.ayataka.kordis.event.events.ServerReadyEvent
import java.awt.Color
import java.time.Instant

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

        while (true) {
            channel.send {
                title = "title ~~(did you know you can have markdown here too?)~~"
                description = "this supports [named links](https://discordapp.com) on top of the previously shown subset of markdown. ```\\nyes, even code blocks```"
                url = "https://discordapp.com"
                color = Color(0x403FFF)
                timestamp = Instant.now()
                thumbnailUrl = "https://cdn.discordapp.com/embed/avatars/0.png"
                imageUrl = "https://cdn.discordapp.com/embed/avatars/0.png"

                footer {
                    text = "this is footer"
                    iconUrl = "https://cdn.discordapp.com/embed/avatars/0.png"
                }

                author {
                    name = "author name"
                    url = "https://discordapp.com"
                    iconUrl = "https://cdn.discordapp.com/embed/avatars/0.png"
                }

                field {
                    name = "\uD83E\uDD14"
                    value = "some of these properties have certain limits..."
                }

                field { name = "\uD83E\uDD14"; value = "try exceeding some of them!" }

                field {
                    name = "\uD83E\uDD14"
                    value = "an informative error should show up, and this view will remain as-is until all issues are fixed"
                }

                field {
                    name = "\uD83E\uDD14"
                    value = "these last two"
                    inline = true
                }

                field {
                    name = "\uD83E\uDD14"
                    value = "are inline fields"
                    inline = true
                }
            }
        }
    }
}