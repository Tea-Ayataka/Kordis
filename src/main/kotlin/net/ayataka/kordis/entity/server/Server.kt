package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.server.ban.Ban
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.announcement.AnnouncementChannel
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.store.StoreChannel
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
     * Whether the server is ready
     */
    val ready: Boolean

    /**
     * The count of server members
     */
    val memberCount: Int

    /**
     * The id of server owner
     */
    val ownerId: Long

    /**
     * The url of the server icon
     */
    val icon: Image?

    /**
     * The url of the server splash
     */
    val splash: Image?

    /**
     * the voice region of the server
     */
    val region: Region

    /**
     * the AFK channel of the server
     */
    val afkChannel: ServerVoiceChannel?

    /**
     * The afk timeout in seconds
     */
    val afkTimeout: Int

    /**
     * The verification level of the server
     */
    val verificationLevel: VerificationLevel

    /**
     * The default message notification level of the server
     */
    val defaultMessageNotificationLevel: MessageNotificationLevel

    /**
     * The explicit content filter level of the server
     */
    val explicitContentFilterLevel: ExplicitContentFilterLevel

    /**
     * The Multi-Factor-Authorization level of the server
     */
    val mfaLevel: MfaLevel

    /**
     * The roles on the server
     */
    val roles: NameableEntitySet<Role>

    /**
     * The emojis on the server
     */
    val emojis: NameableEntitySet<Emoji>

    /**
     * The text channels on the server
     */
    val textChannels: NameableEntitySet<ServerTextChannel>

    /**
     * The voice channels on the server
     */
    val voiceChannels: NameableEntitySet<ServerVoiceChannel>

    /**
     * The channel categories on the server
     */
    val channelCategories: NameableEntitySet<ChannelCategory>

    /**
     * The announcement channels on the server
     */
    val announcementChannels: NameableEntitySet<AnnouncementChannel>

    /**
     * The store channels on the server
     */
    val storeChannels: NameableEntitySet<StoreChannel>

    /**
     * The all channels on the server
     */
    val channels: NameableEntitySet<ServerChannel>

    /**
     * Get the server owner
     */
    suspend fun owner() = findMember(ownerId)!!

    /**
     * Find a member of the server by id
     */
    suspend fun findMember(userId: Long) : Member?

    /**
     * Find members of the server by id
     */
    suspend fun findMembers(vararg userIds: Long) : List<Member>

    /**
     * Find members of the server by name
     */
    suspend fun findMembers(query: String) : List<Member>

    /**
     * Download entire server members
     */
    suspend fun getMembers() : List<Member>

    /**
     * Get server bans
     */
    suspend fun bans(): Collection<Ban>

    /**
     * Kick a member from the server
     */
    suspend fun kick(member: Member)

    /**
     * Ban a user from the server
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String? = null)

    /**
     * Unban a user from the server
     */
    suspend fun unban(user: User)

    /**
     * Edit the server
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

    /**
     * Get the audit logs
     */
    // TODO: Not implemented

    /**
     * Leave from the server
     */
    suspend fun leave()
}