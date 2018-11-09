package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.addListener
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.permission.permissions
import net.ayataka.kordis.event.EventListener
import net.ayataka.kordis.event.events.ServerReadyEvent
import net.ayataka.kordis.event.events.message.MessageReceiveEvent
import net.ayataka.kordis.utils.formatAsDate
import java.awt.Color
import java.time.Instant
import kotlin.random.Random

fun main(args: Array<String>) = runBlocking {
    TestBot().start(args[0])
}

class TestBot {
    suspend fun start(token: String) {
        val client = Kordis.create {
            this.token = token
            addListener(this@TestBot)
        }

        client.servers.findByName("TestServer")
        client.servers.find(9999)
        client.servers.forEach {
            println(it.name)
        }

        client.users.find(-1)

        delay(99999999999999)
    }

    @EventListener
    suspend fun onMessageReceive(event: MessageReceiveEvent) {
        val author = event.message.author ?: return
        val server = event.server ?: return
        val channel = event.message.serverChannel ?: return
        val text = event.message.content

        if (author.name != "Ayataka") {
            return
        }

        if (text == "!serverinfo") {
            channel.send {
                author(name = server.name)
                field("ID", server.id)
                field("Server created", server.timestamp.formatAsDate(), true)
                field("Members", server.members.joinToString { it.name }, true)
                field("Text channels", server.textChannels.joinToString { it.name })
                field("Voice channels", server.voiceChannels.joinToString { it.name }.ifEmpty { "None" })
                field("Emojis", server.emojis.size, true)
                field("Roles", server.roles.joinToString { it.name }, true)
                field("Owner", server.owner!!.mention, true)
                field("Region", server.region.displayName, true)
            }
        }

        if (text == "!channelinfo") {
            channel.send {
                author(name = channel.name)
                field("ID", channel.id)
                field("Channel created", channel.timestamp.formatAsDate(), true)
                field("Position", channel.position, true)
                field("ChannelCategory", channel.channelCategory?.name ?: "None", true)
                field("Topic", channel.topic.ifEmpty { "Empty" }, true)
                field("NSFW", channel.nsfw.toString(), true)
                field("Permission Overwrites (User)", channel.userPermissionOverwrites.size, true)
                field("Permission Overwrites (Role)", channel.rolePermissionOverwrites.size, true)
            }

            event.server?.edit {
                name = "VeryName"
            }
        }

        if (text.startsWith("!roleinfo")) {
            val role = server.roles.findByName(text.split(" ")[1])!!

            channel.send {
                author(role.name)
                field("Permissions", role.permissions.toString())
                field("Position", role.position)
                field("Color", role.color.rgb)
                field("hoist", role.hoist.toString())
                field("mentionable", role.mentionable.toString())
                field("managed", role.managed.toString())
            }
        }
    }

    @EventListener
    suspend fun onServerReady(event: ServerReadyEvent) {
        println("Server Ready! ${event.server.name}")
        println("Members:")
        event.server.members.find(9999999)
        event.server.members.forEach {
            println("Name: ${it.tag}, Roles: ${it.roles.joinToString()}}")
        }
        println("Roles:")
        event.server.roles.forEach {
            println("Name: ${it.name}, Pos: ${it.position}")
        }
        println("Text Channels:")
        event.server.textChannels.forEach {
            println(it)
        }

        val channel = event.server.textChannels.findByName("aaasaaaaa")

        channel?.delete()
        channel?.send {
            title = "title ~~(did you know you can have markdown here too?)~~"
            description = "this supports [named links](https://discordapp.com) on top of the previously shown subset of markdown. ```\\nyes, even code blocks```"
            url = "https://discordapp.com"
            color = Color(0x403FFF)
            timestamp = Instant.now()
            thumbnailUrl = "https://cdn.discordapp.com/embed/avatars/0.png"
            imageUrl = "https://cdn.discordapp.com/embed/avatars/0.png"

            footer("this is footer", "https://cdn.discordapp.com/embed/avatars/0.png")
            author("author name", "https://discordapp.com", "https://cdn.discordapp.com/embed/avatars/0.png")

            field("\uD83E\uDD14", "some of these properties have certain limits...")
            field("\uD83E\uDD14", "try exceeding some of them!")
            field("\uD83E\uDD14", "an informative error should show up, and this view will remain as-is until all issues are fixed")
            field("\uD83E\uDD14", "these last two", true)
            field("\uD83E\uDD14", "are inline fields", true)
        }

        event.server.roles.findByName("Premium")?.edit {
            color = Color(Random.nextInt(0xFFFFFF))
            permissions = permissions(Permission.ADMINISTRATOR)
        }
    }
}