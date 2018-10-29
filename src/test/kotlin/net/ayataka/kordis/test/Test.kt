package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.ayataka.kordis.DiscordClient

fun main(args: Array<String>) {
    runBlocking {
        DiscordClient().connect("NTAyNDA2NzgzOTU3MjA0OTkz.Dqny3g.p9mydklqhcgT3-saaJprQH-zwQo")
        delay(99999999999999)
    }
}