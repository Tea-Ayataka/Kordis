package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.Region
import net.ayataka.kordis.entity.server.enums.VerificationLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Test {
    @Test
    fun start() = runBlocking {
        val client = Kordis.create { token = System.getProperty("token") }

        val server = withTimeout(10 * 1000) {
            while (true) {
                client.servers.find(515273924662001684)?.let { return@withTimeout it }
                delay(100)
            }
        } as Server

        while (!server.ready) {
        }

        assertEquals("TestServer", server.name)
        assertEquals(Region.US_CENTRAL, server.region)
        assertEquals(515274879679987742, server.afkChannel?.id)
        assertEquals(300, server.afkTimeout)
        assertEquals(VerificationLevel.MEDIUM, server.verificationLevel)

        assertEquals(1, server.emojis.size)
        assertEquals(5, server.roles.size)
        assertEquals(8, server.textChannels.size)
        assertEquals(5, server.voiceChannels.size)
        assertEquals(5, server.channelCategories.size)

        val textChannelsCategory = server.channelCategories.findByName("Text Channels", true)
        assertNotNull(textChannelsCategory)
        assertEquals(515273924662001685, textChannelsCategory.id)
        assertEquals(6, server.textChannels.filter { it.category == textChannelsCategory }.size)

        val user = client.getUser(client.botUser.id)
        assertNotNull(user)

        assertEquals(client.botUser.name, user.name)
        assertEquals(client.botUser.discriminator, user.discriminator)
        assertEquals(client.botUser.tag, user.tag)
    }
}