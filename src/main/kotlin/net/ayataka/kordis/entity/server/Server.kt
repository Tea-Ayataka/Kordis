package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.image.Icon
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.ServerVoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.updater.ServerUpdater
import net.ayataka.kordis.entity.user.User

interface Server : Nameable, Entity {
    /**
     * The name of this server
     */
    override val name: String

    /**
     * The members in this server
     */
    val members: NameableEntitySet<Member>

    /**
     * The url of the server icon
     */
    val icon: Icon?

    /**
     * The url of the server splash
     */
    val splash: Icon?

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
    val afkChannel: ServerTextChannel?

    /**
     * The afk timeout in seconds
     */
    val afkTimeout: Int

    /**
     * The verification level of this server
     */
    val verificationLevel: VerificationLevel

    /**
     * The default message notification level of this server
     */
    val defaultMessageNotificationLevel: MessageNotificationLevel

    /**
     * The explicit content filter level of this server
     */
    val explicitContentFilterLevel: ExplicitContentFilterLevel

    /**
     * The Multi-Factor-Authorization level of this server
     */
    val mfaLevel: MfaLevel

    /**
     * The roles on this server
     */
    val roles: NameableEntitySet<Role>

    /**
     * The emojis on this server
     */
    val emojis: NameableEntitySet<Emoji>

    /**
     * The text channels on this server
     */
    val textChannels: NameableEntitySet<ServerTextChannel>

    /**
     * The voice channels on this server
     */
    val voiceChannels: NameableEntitySet<ServerVoiceChannel>

    /**
     * The channel categories on this server
     */
    val categories: NameableEntitySet<Category>

    /**
     * Kick a member from this server
     */
    suspend fun kick(member: Member)

    /**
     * Ban a user from this server
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String? = null)

    /**
     * Unban a user from this server
     */
    suspend fun unban(user: User)

    /**
     * Edit this server
     *
     * Requires Manage Server permission
     */
    suspend fun edit(block: ServerUpdater.() -> Unit)
}