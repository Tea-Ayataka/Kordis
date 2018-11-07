package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.collection.MemberSetImpl
import net.ayataka.kordis.entity.collection.NameableEntitySetImpl
import net.ayataka.kordis.entity.server.channel.*
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl

class ServerImpl(client: DiscordClientImpl, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    private var ownerId: Long = -1

    override var name = ""
    override var iconHash: String? = null
    override var splashHash: String? = null

    override var region = Region.UNKNOWN
    override var afkChannel: ServerTextChannel? = null
    override var afkTimeout = -1
    override var verificationLevel = VerificationLevel.NONE
    override var defaultMessageNotificationLevel = MessageNotificationLevel.ALL_MESSAGES
    override var explicitContentFilterLevel = ExplicitContentFilterLevel.DISABLED
    override var mfaLevel = MfaLevel.NONE

    override val roles = NameableEntitySetImpl<Role>()
    override val emojis = NameableEntitySetImpl<Emoji>()
    override val textChannels = NameableEntitySetImpl<ServerTextChannel>()
    override val voiceChannels = NameableEntitySetImpl<ServerVoiceChannel>()
    override val categories = NameableEntitySetImpl<Category>()
    override val members = MemberSetImpl()

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

    fun update(json: JsonObject) {
        name = json["name"].content
        ownerId = json["owner_id"].long
        defaultMessageNotificationLevel = MessageNotificationLevel.values()[json["default_message_notifications"].int]
        iconHash = json["icon"].content
        afkTimeout = json["afk_timeout"].int

        roles.clear()
        roles.addAll(json["roles"].jsonArray.map { RoleImpl(client, it.jsonObject) })

        members.clear()
        members.addAll(json["members"].jsonArray.map { MemberImpl(client, it.jsonObject, this, UserImpl(client, it.jsonObject["user"].jsonObject)) })

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
}