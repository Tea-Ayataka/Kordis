package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.VoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.user.Member
import net.ayataka.kordis.entity.user.User

interface Server : Entity {
    /**
     * The name of this server
     */
    val name: String

    /**
     * The members in this server
     */
    val members: Collection<Member>

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

    val roles: Collection<Role>

    val emojis: Collection<Emoji>

    val mfaLevel: MfaLevel

    val textChannels: Collection<TextChannel>

    val voiceChannels: Collection<VoiceChannel>

    val categories: Collection<Category>
}