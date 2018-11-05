package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.collection.MemberSet
import net.ayataka.kordis.entity.collection.NameableIterableEntitySet
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.ServerVoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.user.User

interface Server : Nameable, Entity {
    /**
     * The name of this server
     */
    override val name: String

    /**
     * The members in this server
     */
    val members: MemberSet

    /**
     * The hash id of the server icon
     */
    val iconHash: String?

    /**
     * The hash id of the server splash
     */
    val splashHash: String?

    /**
     * The owner of this server
     * Possibly null if the user isn't cached during reconnect
     */
    val owner: User?

    /**
     * the voice region of this server
     */
    val region: Region

    /**
     * the AFK channel of this server
     */
    val afkChannel: TextChannel?

    /**
     * The afk timeout in seconds
     */
    val afkTimeout: Int

    /**
     * The verification level of this server
     */
    val verificationLevel: VerificationLevel

    val defaultMessageNotificationLevel: MessageNotificationLevel

    val explicitContentFilterLevel: ExplicitContentFilterLevel

    val roles: NameableIterableEntitySet<Role>

    val emojis: NameableIterableEntitySet<Emoji>

    val mfaLevel: MfaLevel

    val textChannels: NameableIterableEntitySet<ServerTextChannel>

    val voiceChannels: NameableIterableEntitySet<ServerVoiceChannel>

    val categories: NameableIterableEntitySet<Category>

    suspend fun edit(
            //name: String = client.serverMap[id]!!.name,
            //iconHash: String = client.serverMap[id],
            // a: String = client.serverMap.elements().toList()

    ) {

    }
}