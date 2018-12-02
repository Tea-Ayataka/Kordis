package net.ayataka.kordis.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.Kordis
import net.ayataka.kordis.entity.botUser
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.enums.Region
import net.ayataka.kordis.entity.server.enums.VerificationLevel
import net.ayataka.kordis.event.events.server.user.UserRoleUpdateEvent
import net.ayataka.kordis.exception.MissingPermissionsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class Test {
    lateinit var client: DiscordClient

    @Test
    fun start() = runBlocking {
        var userRoleUpdateEvent: UserRoleUpdateEvent? = null

        client = Kordis.create {
            token = System.getProperty("token")

            addHandler<UserRoleUpdateEvent> {
                userRoleUpdateEvent = it
                println("Role Update: ${it.member.tag}, before: '${it.before.joinToString { it.name }}', after: '${it.member.roles.joinToString { it.name }}'")
            }
        }

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
        assertEquals(6, server.roles.size)
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

        val role = server.roles.findByName("Administrator")
        assertNotNull(role)

        assert(assertFails { runBlocking { role.delete() } } is MissingPermissionsException)

        // Test User Role Update Event
        val mutedRole = server.roles.findByName("Muted")
        assertNotNull(mutedRole)

        if (server.members.botUser.roles.contains(mutedRole)) {
            server.members.botUser.removeRole(mutedRole)

            while (mutedRole in server.members.botUser.roles) {
            }
        }

        val rolesBefore = server.members.botUser.roles.map { it.id }
        server.members.botUser.addRole(mutedRole)

        while (userRoleUpdateEvent == null || mutedRole in userRoleUpdateEvent!!.before) {
        }

        assert(userRoleUpdateEvent!!.before.map { it.id }.containsAll(rolesBefore))
        assert(mutedRole in server.members.botUser.roles)
    }
}