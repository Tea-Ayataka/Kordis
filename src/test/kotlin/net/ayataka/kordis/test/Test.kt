package net.ayataka.kordis.test

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.addHandler
import net.ayataka.kordis.entity.botUser
import net.ayataka.kordis.entity.deleteAll
import net.ayataka.kordis.entity.permissions
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.event.EventHandler
import net.ayataka.kordis.event.events.message.MessageEditEvent
import net.ayataka.kordis.event.events.message.MessageReceiveEvent
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.event.events.server.user.UserJoinEvent
import net.ayataka.kordis.utils.formatAsDate
import java.awt.Color
import java.io.File
import java.time.Instant
import kotlin.random.Random

fun main(args: Array<String>) = runBlocking {
    TestBot().start(args[0])
}

class TestBot {
    suspend fun start(token: String) {
        val client = Kordis.create {
            this.token = token
            shard = 0
            maxShard = 1
            addListener(this@TestBot)
            addHandler<MessageReceiveEvent> {
                println(it.message.content.reversed())
            }

            addHandler<UserJoinEvent> {
                println(it.member.name + " has joined")
            }
        }

        client.updateStatus(UserStatus.ONLINE, ActivityType.PLAYING, "Kordis v0.0.1-SNAPSHOT")

        client.addHandler<MessageEditEvent> {
            println("Edited! ${it.message}")
        }

        client.servers.findByName("TestServer")
        client.servers.find(9999)
        client.servers.forEach {
            println(it.name)
        }

        client.users.find(-1)
    }

    @EventHandler
    suspend fun onMessageReceive(event: MessageReceiveEvent) {
        val author = event.message.member ?: return
        val server = event.server ?: return
        val channel = event.message.serverChannel ?: return
        val text = event.message.content

        if (author.name != "Ayataka") {
            return
        }

        if (text == "!serverinfo") {
            channel.send {
                embed {
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
        }

        if (text == "!channelinfo") {
            channel.send {
                embed {
                    author(name = channel.name)
                    field("ID", channel.id)
                    field("Channel created", channel.timestamp.formatAsDate(), true)
                    field("Position", channel.position, true)
                    field("ChannelCategory", channel.category?.name ?: "None", true)
                    field("Topic", channel.topic ?: "real null", true)
                    field("NSFW", channel.nsfw.toString(), true)
                    field("Permission Overwrites (User)", channel.userPermissionOverwrites.size, true)
                    field("Permission Overwrites (Role)", channel.rolePermissionOverwrites.size, true)
                }
            }
        }

        if (text.startsWith("!roleinfo")) {
            val role = server.roles.findByName(text.split(" ")[1])!!

            channel.send {
                embed {
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

        if (text.startsWith("!purge")) {
            val amount = text.split(" ")[1].toInt()
            channel.getMessages(amount).deleteAll()
            channel.send("Deleted $amount messages")
        }

        if (text.startsWith("!ping")) {
            val time = System.currentTimeMillis()
            channel.send("Pong!").edit("Pong! `${System.currentTimeMillis() - time} ms`")
        }

        if (text.startsWith("!stress")) {
            repeat(30) {
                channel.getMessage(511442836198129715)
            }
        }

        if (text.startsWith("!emojis")) {
            server.emojis.forEach {
                channel.send("Name: ${it.name}, Image: ${it.image.url}")
            }
        }

        if (text.startsWith("!delete-emojis")) {
            server.emojis.forEach { it.delete() }
        }

        if (text.startsWith("!create-emoji")) {
            server.createEmoji {
                name = "Thonkers"
                image = File("thonker.png").readBytes()
            }
        }

        if (text.startsWith("!presences")) {
            channel.send {
                server.members.forEach {
                    appendLine("${it.tag} ${it.status}")
                }
            }
        }

        if (text.startsWith("!userinfo")) {
            val id = text.split(" ")[1].toLong()
            val user = channel.client.getUser(id)
            channel.send(user.toString())
        }

        if (text.startsWith("!dmme")) {
            author.getPrivateChannel().send("Hey :3")
        }

        if (text.startsWith("!setnick")) {
            server.members.botUser.setNickname("Hello World")
        }

        if (text.startsWith("!spam")) {
            channel.send("Huh?")
            repeat(50) {
                GlobalScope.launch {
                    channel.send("Spam")
                }
            }
        }

        if (text.startsWith("!clearnick")) {
            server.members.botUser.setNickname(null)
        }

        if (text.startsWith("!test")) {
            val serverOne = server.client.servers.find(447988883293077507)!!
            val serverTwo = server.client.servers.find(446923846562611203)!!

            val memberOne = serverOne.members.find(371868794043236364)
            val memberTwo = serverTwo.members.find(371868794043236364)

            val user: User? = memberOne
            val realUser = server.client.users.find(371868794043236364)!!

            println("member(1) == fakeuser : ${memberOne == user}")
            println("fake == member(1) : ${user == memberOne}")
            println("member(1) == user : ${memberOne == realUser}")
            println("user == member(1) : ${realUser == memberOne}")
            println("member(1) == member(2) : ${memberOne == memberTwo}")
            println("member(2) == member(1) : ${memberTwo == memberOne}")
        }

        if (text.startsWith("!addrole")) {
            server.roles.findByName("Test")?.let {
                author.addRole(it)
            }
        }
    }

    @EventHandler
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
        println("Bans:")
        event.server.bans().forEach {
            println(it)
        }

        val channel = event.server.textChannels.findByName("aaasaaaaa")

        channel?.delete()
        channel?.send {
            embed {
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
        }

        event.server.roles.findByName("not found")?.edit {
            color = Color(Random.nextInt(0xFFFFFF))
            permissions = permissions(Permission.ADMINISTRATOR)
        }
    }
}