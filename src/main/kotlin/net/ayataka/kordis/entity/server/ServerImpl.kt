package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.VoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.user.Member
import net.ayataka.kordis.entity.user.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl

class ServerImpl(client: DiscordClient, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    private var ownerId: Long = -1

    override var iconHash: String? = null
    override var splashHash: String? = null
    override val owner
        get() = client.users[ownerId]
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
    override var voiceChannels = mutableSetOf<VoiceChannel>()
    override var categories = mutableSetOf<Category>()
    override var name = ""
    override var members = mutableSetOf<Member>()

    init {
        update(json)

        synchronized(client.servers) {
            if (client.servers[id] != null) {
                throw IllegalStateException("This server is already initialized!")
            }

            client.servers[id] = this
        }
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        ownerId = json["owner_id"].long
        defaultMessageNotificationLevel = MessageNotificationLevel.values()[json["default_message_notifications"].int]
        iconHash = json["icon"].content
        afkTimeout = json["afk_timeout"].int

        roles = json["roles"].jsonArray.map { RoleImpl(client, it.jsonObject) }.toMutableSet()
        members = json["members"].jsonArray.map { MemberImpl(client, it.jsonObject, this, UserImpl(client, it.jsonObject["user"].jsonObject)) }.toMutableSet()
    }

    override fun getRoleById(id: Long): Role? {
        return roles.find { it.id == id }
    }
}