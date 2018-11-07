package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.server.channel.*
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.rest.Endpoint

class ServerImpl(client: DiscordClientImpl, json: JsonObject) : Server, Updatable, DiscordEntity(client, json["id"].long) {
    @Volatile private var ownerId: Long = -1

    @Volatile override var name = ""
    @Volatile override var iconHash: String? = null
    @Volatile override var splashHash: String? = null

    @Volatile override var region = Region.UNKNOWN
    @Volatile override var afkChannel: ServerTextChannel? = null
    @Volatile override var afkTimeout = -1
    @Volatile override var verificationLevel = VerificationLevel.NONE
    @Volatile override var defaultMessageNotificationLevel = MessageNotificationLevel.ALL_MESSAGES
    @Volatile override var explicitContentFilterLevel = ExplicitContentFilterLevel.DISABLED
    @Volatile override var mfaLevel = MfaLevel.NONE

    override val roles = NameableEntitySetImpl<Role>()
    override val emojis = NameableEntitySetImpl<Emoji>()
    override val textChannels = NameableEntitySetImpl<ServerTextChannel>()
    override val voiceChannels = NameableEntitySetImpl<ServerVoiceChannel>()
    override val categories = NameableEntitySetImpl<Category>()
    override val members = NameableEntitySetImpl<Member>()

    override val owner
        get() = client.users.find(ownerId)

    init {
        update(json)

        synchronized(client.servers) {
            if (!client.servers.add(this)) {
                throw IllegalStateException("This server is already initialized!")
            }
        }
    }

    override fun update(json: JsonObject) {
        name = json["name"].content
        ownerId = json["owner_id"].long
        defaultMessageNotificationLevel = MessageNotificationLevel.values()[json["default_message_notifications"].int]
        iconHash = json["icon"].content
        afkTimeout = json["afk_timeout"].int

        roles.clear()
        roles.addAll(json["roles"].jsonArray.map { RoleImpl(client, it.jsonObject, this) })

        members.clear()
        members.addAll(json["members"].jsonArray.map {
            val userData = it.jsonObject["user"].jsonObject

            MemberImpl(
                    client,
                    it.jsonObject,
                    this,
                    client.users.getOrPut(userData["id"].long) {
                        UserImpl(client, userData)
                    })
        })

        val channels = json["channels"].jsonArray

        // Load categories first
        categories.clear()
        categories.addAll(
                channels.filter { it.jsonObject["type"].int == ChannelType.GUILD_CATEGORY.id }
                        .map { CategoryImpl(this, client, it.jsonObject) }
        )

        // Load text channels
        textChannels.clear()
        textChannels.addAll(
                channels.filter { it.jsonObject["type"].int == ChannelType.GUILD_TEXT.id }
                        .map { ServerTextChannelImpl(this, client, it.jsonObject) }
        )

        // Load voice channels
        voiceChannels.clear()
        voiceChannels.addAll(
                channels.filter { it.jsonObject["type"].int == ChannelType.GUILD_VOICE.id }
                        .map { VoiceChannelImpl(this, client, it.jsonObject) }
        )
    }

    override suspend fun kick(member: Member) {
        checkPermission(this, Permission.KICK_MEMBERS)
        checkManageable(member)

        client.rest.request(
                Endpoint.REMOVE_GUILD_MEMBER.format(mapOf("guild.id" to id, "user.id" to member.id))
        )
    }
}