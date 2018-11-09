package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.collection.find
import net.ayataka.kordis.entity.image.Icon
import net.ayataka.kordis.entity.image.IconImpl
import net.ayataka.kordis.entity.server.channel.*
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.updater.ServerUpdater
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.base64

class ServerImpl(client: DiscordClientImpl, json: JsonObject) : Server, Updatable, DiscordEntity(client, json["id"].long) {
    @Volatile override var name = ""
    @Volatile override var icon: Icon? = null
    @Volatile override var splash: Icon? = null
    @Volatile override var owner: User? = null
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
        icon = json["icon"].contentOrNull?.let { IconImpl.server(id, it) }
        splash = json["splash"].contentOrNull?.let { IconImpl.splash(id, it) }
        region = Region[json["region"].content]
        afkTimeout = json["afk_timeout"].int
        verificationLevel = VerificationLevel[json["verification_level"].int]
        defaultMessageNotificationLevel = MessageNotificationLevel[json["default_message_notifications"].int]
        explicitContentFilterLevel = ExplicitContentFilterLevel[json["explicit_content_filter"].int]
        mfaLevel = MfaLevel[json["mfa_level"].int]

        // Update roles
        val roleObjects = json["roles"].jsonArray.map { it.jsonObject }
        val roleIds = roleObjects.map { it["id"].long }

        roles.removeIf { it.id !in roleIds }
        roleObjects.forEach {
            roles.updateOrPut(it["id"].long, it) { RoleImpl(client, it, this) }
        }

        // Update members
        if (json.containsKey("members")) {
            val memberObjects = json["members"].jsonArray.map { it.jsonObject }
            val memberIds = memberObjects.map { it["user"].jsonObject["id"].long }

            members.removeIf { it.id !in memberIds }
            memberObjects.forEach {
                val userObject = it["user"].jsonObject
                val userId = userObject["id"].long

                members.updateOrPut(userId, it) {
                    MemberImpl(
                            client, it, this,
                            client.users.getOrPut(userId) {
                                UserImpl(client, userObject)
                            })
                }
            }
        }

        if (json.containsKey("channels")) {
            val channelObjects = json["channels"].jsonArray.map { it.jsonObject }
            val channelIds = channelObjects.map { it["id"].long }

            // Clear deleted channels
            categories.removeIf { it.id !in channelIds }
            textChannels.removeIf { it.id !in channelIds }
            voiceChannels.removeIf { it.id !in channelIds }

            // Load categories first
            channelObjects.filter { it["type"].int == ChannelType.GUILD_CATEGORY.id }.forEach {
                categories.updateOrPut(it["id"].long, it) { CategoryImpl(this, client, it) }
            }

            // Load text channels
            channelObjects.filter { it["type"].int == ChannelType.GUILD_TEXT.id }.forEach {
                textChannels.updateOrPut(it["id"].long, it) { ServerTextChannelImpl(this, client, it) }
            }

            // Load voice channels
            channelObjects.filter { it["type"].int == ChannelType.GUILD_VOICE.id }.forEach {
                voiceChannels.updateOrPut(it["id"].long, it) { VoiceChannelImpl(this, client, it) }
            }
        }

        // Initialize after loading other entities
        afkChannel = json["afk_channel_id"].longOrNull?.let { textChannels.find(it) }
        owner = client.users.find(json["owner_id"].long)
    }

    override suspend fun kick(member: Member) {
        checkPermission(this, Permission.KICK_MEMBERS)
        checkManageable(member)

        client.rest.request(
                Endpoint.REMOVE_GUILD_MEMBER.format(mapOf("guild.id" to id, "user.id" to member.id))
        )
    }

    override suspend fun ban(user: User, deleteMessageDays: Int, reason: String?) {
        checkPermission(this, Permission.BAN_MEMBERS)
        members.find(user)?.let { checkManageable(it) }

        client.rest.request(
                Endpoint.CREATE_GUILD_BAN.format(mapOf("guild.id" to id, "user.id" to user.id)),
                json {
                    "delete-message-days" to deleteMessageDays
                    if (reason != null && reason.isNotEmpty()) {
                        "reason" to reason
                    }
                }
        )
    }

    override suspend fun unban(user: User) {
        checkPermission(this, Permission.KICK_MEMBERS)

        client.rest.request(
                Endpoint.REMOVE_GUILD_BAN.format(mapOf("guild.id" to id, "user.id" to user.id))
        )
    }

    override suspend fun edit(block: ServerUpdater.() -> Unit) {
        checkPermission(this, Permission.MANAGE_GUILD)

        val updater = ServerUpdater(this).apply(block)

        val json = json {
            if (updater.name != name) {
                "name" to updater.name
            }
            if (updater.icon != null) {
                "icon" to "data:image/png;base64,${updater.icon!!.base64()}"
            }
            if (updater.region != region) {
                "region" to region.name
            }
            if (updater.afkChannel != afkChannel) {
                "afk_channel_id" to updater.afkChannel?.id
            }
            if (updater.afkTimeout != afkTimeout) {
                "afk_timeout" to updater.afkTimeout
            }
            if (updater.defaultMessageNotificationLevel != defaultMessageNotificationLevel) {
                "default_message_notifications" to updater.defaultMessageNotificationLevel.ordinal
            }
            if (updater.explicitContentFilterLevel != explicitContentFilterLevel) {
                "explicit_content_filter" to updater.explicitContentFilterLevel.id
            }
            if (updater.mfaLevel != mfaLevel) {
                "mfa_level" to updater.mfaLevel.id
            }
        }

        if (json.isNotEmpty()) {
            client.rest.request(
                    Endpoint.MODIFY_GUILD.format(mapOf("guild.id" to id)),
                    json
            )
        }
    }
}