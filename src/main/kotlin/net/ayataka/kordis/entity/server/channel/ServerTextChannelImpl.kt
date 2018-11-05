package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.message.EmbedBuilder
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageImpl
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.UserPermissionOverwrite

class ServerTextChannelImpl(
        override val server: Server,
        clientImpl: DiscordClientImpl,
        json: JsonObject
) : ServerTextChannel, DiscordEntity(clientImpl, json["id"].long) {

    override var name = ""
    override var topic: String? = null
    override var isNsfw = false
    override var rateLimitPerUser = -1
    override var position = -1
    override var category: Category? = null
    override val userPermissionOverwrites = mutableSetOf<UserPermissionOverwrite>()
    override val rolePermissionsOverwrites = mutableSetOf<RolePermissionOverwrite>()

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        topic = json["topic"].contentOrNull
        isNsfw = json["nsfw"].boolean
        rateLimitPerUser = json["rate_limit_per_user"].int
        position = json["position"].int
        json["parent_id"].longOrNull?.let {
            category = server.categories.find(it)
        }
    }

    override suspend fun send(text: String): Message {
        return MessageImpl()
    }

    override suspend fun send(block: EmbedBuilder.() -> Unit): Message {
        return MessageImpl()
    }
}