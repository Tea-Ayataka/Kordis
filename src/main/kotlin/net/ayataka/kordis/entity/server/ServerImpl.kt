package net.ayataka.kordis.entity.server

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.botUser
import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.find
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.image.ImageImpl
import net.ayataka.kordis.entity.server.ban.Ban
import net.ayataka.kordis.entity.server.ban.BanImpl
import net.ayataka.kordis.entity.server.channel.ServerChannel
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
import net.ayataka.kordis.entity.server.emoji.EmojiImpl
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.server.role.RoleBuilder
import net.ayataka.kordis.entity.server.role.RoleImpl
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ServerImpl(client: DiscordClientImpl, id: Long) : Server, Updatable, DiscordEntity(client, id) {
    @Volatile var initialized = AtomicBoolean()

    @Volatile override var ready = false
    @Volatile override var name = ""
    @Volatile override var icon: Image? = null
    @Volatile override var splash: Image? = null
    @Volatile override var owner: User? = null
    @Volatile override var region = Region.UNKNOWN
    @Volatile override var afkChannel: ServerVoiceChannel? = null
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

    // For setup
    private val temporallyUserPresences = ConcurrentHashMap<Long, JsonObject>()
    @Volatile
    var memberCount = AtomicInteger()
    @Volatile
    var removedMembers = mutableListOf<Long>()

    override val channels = object : NameableEntitySet<ServerChannel> {
        override val size: Int
            get() = textChannels.size + voiceChannels.size + channelCategories.size

        override fun findByName(text: String, ignoreCase: Boolean) =
                textChannels.findByName(text, ignoreCase) ?: voiceChannels.findByName(text, ignoreCase)
                ?: channelCategories.findByName(text, ignoreCase)

        override fun find(id: Long) =
                textChannels.find(id) ?: voiceChannels.find(id) ?: channelCategories.find(id)

        override fun contains(element: ServerChannel) =
                textChannels.contains(element) || voiceChannels.contains(element) || channelCategories.contains(element)

        override fun containsAll(elements: Collection<ServerChannel>) =
                textChannels.plus(voiceChannels).plus(channelCategories).containsAll(elements)

        override fun isEmpty() =
                textChannels.isEmpty() && voiceChannels.isEmpty() && channelCategories.isEmpty()

        override fun iterator() =
                textChannels.plus(voiceChannels).plus(channelCategories).iterator()
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        icon = json["icon"].asStringOrNull?.let { ImageImpl.server(id, it) }
        splash = json["splash"].asStringOrNull?.let { ImageImpl.splash(id, it) }
        region = Region[json["region"].asString]
        afkTimeout = json["afk_timeout"].asInt
        verificationLevel = VerificationLevel[json["verification_level"].asInt]
        defaultMessageNotificationLevel = MessageNotificationLevel[json["default_message_notifications"].asInt]
        explicitContentFilterLevel = ExplicitContentFilterLevel[json["explicit_content_filter"].asInt]
        mfaLevel = MfaLevel[json["mfa_level"].asInt]

        // Update emojis
        updateEmojis(json)

        // Update roles
        val roleObjects = json["roles"].asJsonArray.map { it.asJsonObject }
        val roleIds = roleObjects.map { it["id"].asLong }

        roles.removeIf { it.id !in roleIds }
        roleObjects.forEach {
            roles.updateOrPut(it["id"].asLong, it) { RoleImpl(this, client, it) }
        }

        json.getOrNull("member_count")?.let {
            memberCount.set(it.asInt)
        }

        // Update members
        json.getOrNull("members")?.let {
            it.asJsonArray.map { it.asJsonObject }.forEach {
                val userObject = it["user"].asJsonObject
                val userId = userObject["id"].asLong

                members.updateOrPut(userId, it) {
                    MemberImpl(
                            client, it, this,
                            client.users.getOrPut(userId) {
                                UserImpl(client, userObject)
                            })
                }
            }
        }

        // Update channels
        json.getOrNull("channels")?.let {
            val objects = it.asJsonArray.map { it.asJsonObject }
            val ids = objects.map { it["id"].asLong }

            // Clear deleted channels
            channelCategories.removeIf { it.id !in ids }
            textChannels.removeIf { it.id !in ids }
            voiceChannels.removeIf { it.id !in ids }

            // Load channelCategories first
            objects.filter { it["type"].asInt == ChannelType.GUILD_CATEGORY.id }.forEach {
                channelCategories.updateOrPut(it["id"].asLong, it) { ChannelCategoryImpl(this, client, it) }
            }

            // Load text channels
            objects.filter { it["type"].asInt == ChannelType.GUILD_TEXT.id }.forEach {
                textChannels.updateOrPut(it["id"].asLong, it) { ServerTextChannelImpl(this, client, it) }
            }

            // Load voice channels
            objects.filter { it["type"].asInt == ChannelType.GUILD_VOICE.id }.forEach {
                voiceChannels.updateOrPut(it["id"].asLong, it) { ServerVoiceChannelImpl(this, client, it) }
            }
        }

        // Update after loading other entities
        // Update presences
        json.getOrNull("presences")?.let { objects ->
            objects.asJsonArray.map { it.asJsonObject }.forEach {
                val userId = it["user"].asJsonObject["id"].asLong
                (members.find(userId) as? MemberImpl)?.updatePresence(it) ?: temporallyUserPresences.put(userId, it)
            }
        }

        afkChannel = json["afk_channel_id"].asLongOrNull?.let { voiceChannels.find(it) }
        owner = client.users.find(json["owner_id"].asLong)
    }

    fun updateEmojis(json: JsonObject) {
        val objects = json["emojis"].asJsonArray.map { it.asJsonObject }
        val ids = objects.map { it["id"].asLong }

        emojis.removeIf { it.id !in ids }
        objects.forEach {
            emojis.updateOrPut(it["id"].asLong, it) { EmojiImpl(this, client, it) }
        }
    }

    fun applyTemporaryPresences() {
        temporallyUserPresences.forEach {
            (members.find(it.key) as? MemberImpl)?.updatePresence(it.value)
        }

        temporallyUserPresences.clear()
    }

    override suspend fun kick(member: Member) {
        checkExistence()
        checkPermission(this, Permission.KICK_MEMBERS)
        checkManageable(member)

        client.rest.request(
                Endpoint.REMOVE_GUILD_MEMBER.format("guild.id" to id, "user.id" to member.id)
        )
    }

    override suspend fun ban(user: User, deleteMessageDays: Int, reason: String?) {
        checkExistence()
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
        checkExistence()
        checkPermission(this, Permission.KICK_MEMBERS)

        client.rest.request(
                Endpoint.REMOVE_GUILD_BAN.format("guild.id" to id, "user.id" to user.id)
        )
    }

    override suspend fun edit(block: ServerBuilder.() -> Unit) {
        checkExistence()
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
                "region" to region.id
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
        checkExistence()

        val response = client.rest.request(Endpoint.GET_GUILD_BANS.format("guild.id" to id)).asJsonArray

        return response.map {
            val reason = it.asJsonObject["reason"].asStringOrNull
            val user = client.users.getOrPut(it.asJsonObject["user"].asJsonObject["id"].asLong) {
                UserImpl(client, it.asJsonObject["user"].asJsonObject)
            }

            BanImpl(reason, user)
        }
    }

    override suspend fun createTextChannel(block: ServerTextChannelBuilder.() -> Unit): ServerTextChannel {
        checkExistence()
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
                "permission_overwrites" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).asJsonObject

        return textChannels.getOrPut(response["id"].asLong) { ServerTextChannelImpl(this, client, response) }
    }

    override suspend fun createVoiceChannel(block: ServerVoiceChannelBuilder.() -> Unit): ServerVoiceChannel {
        checkExistence()
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
                "permission_overwrites" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).asJsonObject

        return voiceChannels.getOrPut(response["id"].asLong) { ServerVoiceChannelImpl(this, client, response) }
    }

    override suspend fun createChannelCategory(block: ServerChannelBuilder.() -> Unit): ChannelCategory {
        checkExistence()
        checkPermission(this, Permission.MANAGE_CHANNELS)

        val builder = ServerChannelBuilder().apply(block)
        val json = json {
            "type" to ChannelType.GUILD_CATEGORY.id
            "name" to (builder.name ?: throw IllegalArgumentException("channel name must be specified"))

            if (builder.position != null) {
                "position" to builder.position
            }

            if (builder.rolePermissionOverwrites.isNotEmpty() || builder.userPermissionOverwrites.isNotEmpty()) {
                "permission_overwrites" to ServerChannelImpl.permissionOverwritesToJson(builder)
            }
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_CHANNEL.format("guild.id" to id),
                json
        ).asJsonObject

        return channelCategories.getOrPut(response["id"].asLong) { ChannelCategoryImpl(this, client, response) }
    }

    override suspend fun createRole(block: RoleBuilder.() -> Unit): Role {
        checkExistence()
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
        ).asJsonObject

        return roles.getOrPut(response["id"].asLong) { RoleImpl(this, client, response) }
    }

    override suspend fun createEmoji(block: EmojiBuilder.() -> Unit): Emoji {
        checkExistence()
        checkPermission(this, Permission.MANAGE_EMOJIS)

        val builder = EmojiBuilder().apply(block)
        val json = json {
            "name" to builder.name
            "image" to "data:image/png;base64,${builder.image!!.base64()}"
        }

        val response = client.rest.request(
                Endpoint.CREATE_GUILD_EMOJI.format("guild.id" to id),
                json
        ).asJsonObject

        return emojis.getOrPut(response["id"].asLong) { EmojiImpl(this, client, response) }
    }

    private fun checkExistence() {
        if (client.servers.find(id) == null) {
            throw NotFoundException()
        }
    }
}