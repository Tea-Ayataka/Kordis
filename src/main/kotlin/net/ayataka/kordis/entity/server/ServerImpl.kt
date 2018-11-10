package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.collection.botUser
import net.ayataka.kordis.entity.collection.find
import net.ayataka.kordis.entity.image.Icon
import net.ayataka.kordis.entity.image.IconImpl
import net.ayataka.kordis.entity.server.ban.Ban
import net.ayataka.kordis.entity.server.ban.BanImpl
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.ServerChannelImpl
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.category.ChannelCategoryImpl
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelBuilder
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannelImpl
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannel
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelBuilder
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannelImpl
import net.ayataka.kordis.entity.server.emoji.Emoji
import net.ayataka.kordis.entity.server.emoji.EmojiBuilder
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.server.role.RoleBuilder
import net.ayataka.kordis.entity.server.role.RoleImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.base64
import net.ayataka.kordis.utils.uRgb

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
    override val channelCategories = NameableEntitySetImpl<ChannelCategory>()
    override val members = NameableEntitySetImpl<Member>()

    init {
        update(json)
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
            roles.updateOrPut(it["id"].long, it) { RoleImpl(this, client, it) }
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
            channelCategories.removeIf { it.id !in channelIds }
            textChannels.removeIf { it.id !in channelIds }
            voiceChannels.removeIf { it.id !in channelIds }

            // Load channelCategories first
            channelObjects.filter { it["type"].int == ChannelType.GUILD_CATEGORY.id }.forEach {
                channelCategories.updateOrPut(it["id"].long, it) { ChannelCategoryImpl(this, client, it) }
            }

            // Load text channels
            channelObjects.filter { it["type"].int == ChannelType.GUILD_TEXT.id }.forEach {
                textChannels.updateOrPut(it["id"].long, it) { ServerTextChannelImpl(this, client, it) }
            }

            // Load voice channels
            channelObjects.filter { it["type"].int == ChannelType.GUILD_VOICE.id }.forEach {
                voiceChannels.updateOrPut(it["id"].long, it) { ServerVoiceChannelImpl(this, client, it) }
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
                Endpoint.REMOVE_GUILD_MEMBER.format("guild.id" to id, "user.id" to member.id)
        )
    }

    override suspend fun ban(user: User, deleteMessageDays: Int, reason: String?) {
        checkPermission(this, Permission.BAN_MEMBERS)
        members.find(user)?.let { checkManageable(it) }

        client.rest.request(
                Endpoint.CREATE_GUILD_BAN.format("guild.id" to id, "user.id" to user.id),
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
                Endpoint.REMOVE_GUILD_BAN.format("guild.id" to id, "user.id" to user.id)
        )
    }

    override suspend fun edit(block: ServerBuilder.() -> Unit) {
        checkPermission(this, Permission.MANAGE_GUILD)

        val updater = ServerBuilder(this).apply(block)

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
            if (updater.verificationLevel != verificationLevel) {
                "verification_level" to updater.verificationLevel
            }
        }

        if (json.isNotEmpty()) {
            client.rest.request(
                    Endpoint.MODIFY_GUILD.format("guild.id" to id),
                    json
            )
        }
    }

    override suspend fun bans(): Collection<Ban> {
        val response = client.rest.request(Endpoint.GET_GUILD_BANS.format("guild.id" to id)).jsonArray

        return response.map {
            val reason = it.jsonObject["reason"].contentOrNull
            val user = client.users.getOrPut(it.jsonObject["user"].jsonObject["id"].long) {
                UserImpl(client, it.jsonObject["user"].jsonObject)
            }

            BanImpl(reason, user)
        }
    }

    override suspend fun createTextChannel(block: ServerTextChannelBuilder.() -> Unit): ServerTextChannel {
        checkPermission(this, Permission.MANAGE_CHANNELS)

        val builder = ServerTextChannelBuilder().apply(block)
        val json = json {
            "type" to ChannelType.GUILD_TEXT.id
            "name" to (builder.name ?: throw IllegalArgumentException("channel name must be specified"))
            "nsfw" to builder.nsfw
            "rate_limit_per_user" to builder.rateLimitPerUser

            if (builder.topic != null && builder.topic!!.isNotEmpty()) {
                "topic" to builder.topic
            }

            if (builder.position != null) {
                "position" to builder.position
            }

            if (builder.category != null) {
                checkManageable(builder.category!!)
                "parent_id" to builder.category!!.id
            }

            if (builder.rolePermissionOverwrites.isNotEmpty() || builder.userPermissionOverwrites.isNotEmpty()) {
                "permission_overwrite" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).jsonObject

        return textChannels.put(ServerTextChannelImpl(this, client, response))
    }

    override suspend fun createVoiceChannel(block: ServerVoiceChannelBuilder.() -> Unit): ServerVoiceChannel {
        checkPermission(this, Permission.MANAGE_CHANNELS)

        val builder = ServerVoiceChannelBuilder().apply(block)
        val json = json {
            "type" to ChannelType.GUILD_VOICE.id
            "name" to (builder.name ?: throw IllegalArgumentException("channel name must be specified"))
            "user_limit" to builder.userLimit

            if (builder.bitrate != null) {
                "bitrate" to builder.bitrate
            }

            if (builder.position != null) {
                "position" to builder.position
            }

            if (builder.category != null) {
                checkManageable(builder.category!!)
                "parent_id" to builder.category!!.id
            }

            if (builder.rolePermissionOverwrites.isNotEmpty() || builder.userPermissionOverwrites.isNotEmpty()) {
                "permission_overwrite" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).jsonObject

        return voiceChannels.put(ServerVoiceChannelImpl(this, client, response))
    }

    override suspend fun createChannelCategory(block: ServerChannelBuilder.() -> Unit): ChannelCategory {
        checkPermission(this, Permission.MANAGE_CHANNELS)

        val builder = ServerVoiceChannelBuilder().apply(block)
        val json = json {
            "type" to ChannelType.GUILD_VOICE.id
            "name" to (builder.name ?: throw IllegalArgumentException("channel name must be specified"))
            "user_limit" to builder.userLimit

            if (builder.bitrate != null) {
                "bitrate" to builder.bitrate
            }

            if (builder.position != null) {
                "position" to builder.position
            }

            if (builder.category != null) {
                checkManageable(builder.category!!)
                "parent_id" to builder.category!!.id
            }

            if (builder.rolePermissionOverwrites.isNotEmpty() || builder.userPermissionOverwrites.isNotEmpty()) {
                "permission_overwrite" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).jsonObject

        return channelCategories.put(ChannelCategoryImpl(this, client, response))
    }

    override suspend fun createRole(block: RoleBuilder.() -> Unit): Role {
        checkPermission(this, Permission.MANAGE_ROLES)

        val builder = RoleBuilder().apply(block)
        val json = json {
            "name" to (builder.name ?: throw IllegalArgumentException("role name must be specified"))

            builder.color?.let {
                "color" to it.uRgb()
            }
            builder.hoist?.let {
                "hoist" to it
            }
            builder.mentionable?.let {
                "mentionable" to it
            }
            builder.permissions?.let {
                "permissions" to it.compile()
            }
            builder.position?.let {
                if (it > members.botUser.roles.map { it.position }.max()!!) {
                    throw IllegalArgumentException("The specified position is higher than the top role i have")
                }
                "position" to it
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_ROLE.format("guild.id" to id),
                json
        ).jsonObject

        return roles.put(RoleImpl(this, client, response))
    }

    override suspend fun createEmoji(block: EmojiBuilder.() -> Unit): Emoji {
        TODO("not implemented")
    }
}