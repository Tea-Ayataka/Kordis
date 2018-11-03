package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.channel.Category
import net.ayataka.kordis.entity.server.channel.TextChannel
import net.ayataka.kordis.entity.server.channel.VoiceChannel
import net.ayataka.kordis.entity.server.enums.*
import net.ayataka.kordis.entity.user.Member
import net.ayataka.kordis.entity.user.User

class ServerImpl(client: DiscordClient, json: JsonObject) : Server, DiscordEntity(client, json["id"].long) {
    override var iconHash: String? = null
    override var splashHash: String? = null
    override var owner: User? = null
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
        name = ""
    }
}