package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.DiscordClient

fun main(args: Array<String>) {
    runBlocking {
        DiscordClient(token = args[0]).connect()
        delay(99999999999999)
    }
}