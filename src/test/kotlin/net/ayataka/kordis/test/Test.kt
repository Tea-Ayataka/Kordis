package net.ayataka.kordis.test

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.ConnectionStatus
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.collection.EntitySet
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.enums.ActivityType
import net.ayataka.kordis.entity.server.enums.UserStatus
import net.ayataka.kordis.entity.user.User
import java.io.File
import java.nio.file.Paths
import kotlin.test.Test

@Suppress("ControlFlowWithEmptyBody")
class Test {
    lateinit var client: DiscordClient

    @Test
    fun start() = runBlocking {

        val fakeClient = DiscordClientImpl("", 0, 0, emptySet())

        val server = ServerImpl(fakeClient, -1)
        val json = File("dump.json").readText(Charsets.UTF_8)
        server.update(Gson().fromJson(json, JsonObject::class.java))

        println("${server.channels.size} channels")
        server.channels.forEach {
            println(it.toString())
        }
    }
}