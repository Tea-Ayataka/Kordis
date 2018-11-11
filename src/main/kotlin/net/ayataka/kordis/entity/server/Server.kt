package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.image.Icon
import net.ayataka.kordis.entity.server.ban.Ban
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelBuilder
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannel
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelBuilder
import net.ayataka.kordis.entity.server.emoji.Emoji
import net.ayataka.kordis.entity.server.emoji.EmojiBuilder
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.server.role.RoleBuilder
import net.ayataka.kordis.entity.user.User

interface Server : Nameable, Entity {
    /**
     * Whether the server is ready or not
     */
    val ready: Boolean

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
     * The members in this server
     */
    val members: NameableEntitySet<Member>

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
     * The channel channelCategories on this server
     */
    val channelCategories: NameableEntitySet<ChannelCategory>

    /**
     * Get server bans
     */
    suspend fun bans(): Collection<Ban>

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
     */
    suspend fun edit(block: ServerBuilder.() -> Unit)

    /**
     * Create a text channel
     */
    suspend fun createTextChannel(block: ServerTextChannelBuilder.() -> Unit): ServerTextChannel

    /**
     * Create a voice channel
     */
    suspend fun createVoiceChannel(block: ServerVoiceChannelBuilder.() -> Unit): ServerVoiceChannel

    /**
     * Create a channel category
     */
    suspend fun createChannelCategory(block: ServerChannelBuilder.() -> Unit): ChannelCategory

    /**
     * Create a role
     */
    suspend fun createRole(block: RoleBuilder.() -> Unit): Role

    /**
     * Create an emoji
     */
    suspend fun createEmoji(block: EmojiBuilder.() -> Unit): Emoji
}