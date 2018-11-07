package net.ayataka.kordis.entity.server

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.ServerVoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.rest.Endpoint

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
    val afkChannel: ServerTextChannel?

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

    val roles: NameableEntitySet<Role>

    val emojis: NameableEntitySet<Emoji>

    val mfaLevel: MfaLevel

    val textChannels: NameableEntitySet<ServerTextChannel>

    val voiceChannels: NameableEntitySet<ServerVoiceChannel>

    val categories: NameableEntitySet<Category>

    /**
     * Kick this member
     */
    suspend fun kick(member: Member)

    /**
     * Ban this user from a server
     */
    suspend fun ban(user: User)

    /**
     * Unban this user from a server
     */
    suspend fun unban(user: User)

    /**
     * Ban this user from a server
     */
    suspend fun ban(server: Server, deleteMessageDays: Int = 0, reason: String? = null)

    suspend fun edit(
            //name: String = client.serverMap[id]!!.name,
            //iconHash: String = client.serverMap[id],
            // a: String = client.serverMap.elements().toList()

    ) {

    }
}