package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.collection.MemberListImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.ServerVoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.user.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl

class ServerImpl(client: DiscordClientImpl, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    private var ownerId: Long = -1

    override var iconHash: String? = null
    override var splashHash: String? = null
    override val owner
        get() = client.users.find(ownerId)
    override var region = Region.UNKNOWN
    override var afkChannel: TextChannel? = null
    override var afkTimeout = -1
    override var verificationLevel = VerificationLevel.NONE
    override var defaultMessageNotificationLevel = MessageNotificationLevel.ALL_MESSAGES
    override var explicitContentFilterLevel = ExplicitContentFilterLevel.DISABLED
    override var roles = mutableSetOf<Role>()
    override var emojis = mutableSetOf<Emoji>()
    override var mfaLevel = MfaLevel.NONE
    override var textChannels = mutableSetOf<TextChannel>()
    override var voiceChannels = mutableSetOf<ServerVoiceChannel>()
    override var categories = mutableSetOf<Category>()
    override var name = ""
    override val members = MemberListImpl()

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

        roles = json["roles"].jsonArray.map { RoleImpl(client, it.jsonObject) }.toMutableSet()

        members.clear()
        members.addAll(json["members"].jsonArray.map { MemberImpl(client, it.jsonObject, this, UserImpl(client, it.jsonObject["user"].jsonObject)) })
    }

    override fun getRoleById(id: Long): Role? {
        return roles.find { it.id == id }
    }
}