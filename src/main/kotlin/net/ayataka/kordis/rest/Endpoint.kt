package net.ayataka.kordis.rest

import io.ktor.http.HttpMethod

const val BASE = "https://discordapp.com/api/v6"

sealed class Endpoint(val method: HttpMethod, path: String) {
    open val channel_id: Long? = null
    open val guild_id: Long? = null
    open val webhook_id: Long? = null

    val url = BASE + path

    private val majorParams by lazy {
        listOfNotNull(
            channel_id,
            guild_id,
            webhook_id
        ).map { it.toString() }
    }

    fun majorHash(): Int {
        var result = hashCode()
        result = 31 * result + majorParams.hashCode()
        return result
    }

    class GET_GUILD_AUDIT_LOG(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/audit-logs")
    class GET_CHANNEL(override val channel_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id")
    class MODIFY_CHANNEL_PUT(override val channel_id: Long): Endpoint(HttpMethod.Put, "/channels/$channel_id")
    class MODIFY_CHANNEL_PATCH(override val channel_id: Long): Endpoint(HttpMethod.Patch, "/channels/$channel_id")
    class DELETE_CHANNEL(override val channel_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id")
    class EDIT_CHANNEL_PERMISSIONS(override val channel_id: Long, val overwrite_id: Long): Endpoint(HttpMethod.Put, "/channels/$channel_id/permissions/$overwrite_id")
    class DELETE_CHANNEL_PERMISSION(override val channel_id: Long, val overwrite_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id/permissions/$overwrite_id")
    class GET_CHANNEL_INVITES(override val channel_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id/invites")
    class CREATE_CHANNEL_INVITE(override val channel_id: Long): Endpoint(HttpMethod.Post, "/channels/$channel_id/invites")
    class GET_CHANNEL_MESSAGE(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id/messages/$message_id")
    class GET_CHANNEL_MESSAGES(override val channel_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id/messages")
    class CREATE_MESSAGE(override val channel_id: Long): Endpoint(HttpMethod.Post, "/channels/$channel_id/messages")
    class EDIT_MESSAGE(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Patch, "/channels/$channel_id/messages/$message_id")
    class DELETE_MESSAGE(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id/messages/$message_id")
    class BULK_DELETE_MESSAGES(override val channel_id: Long): Endpoint(HttpMethod.Post, "/channels/$channel_id/messages/bulk-delete")
    class GET_REACTIONS(override val channel_id: Long, val message_id: Long, val emoji: String): Endpoint(HttpMethod.Get, "/channels/$channel_id/messages/$message_id/reactions/$emoji")
    class CREATE_REACTION(override val channel_id: Long, val message_id: Long, val emoji: String): Endpoint(HttpMethod.Put, "/channels/$channel_id/messages/$message_id/reactions/$emoji/@me")
    class DELETE_OWN_REACTION(override val channel_id: Long, val message_id: Long, val emoji: String): Endpoint(HttpMethod.Delete, "/channels/$channel_id/messages/$message_id/reactions/$emoji/@me")
    class DELETE_USER_REACTION(override val channel_id: Long, val message_id: Long, val emoji: String, val user_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id/messages/$message_id/reactions/$emoji/$user_id")
    class DELETE_ALL_REACTIONS(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id/messages/$message_id/reactions")
    class GET_PINNED_MESSAGES(override val channel_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id/pins")
    class ADD_PINNED_CHANNEL_MESSAGE(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Put, "/channels/$channel_id/pins/$message_id")
    class DELETE_PINNED_CHANNEL_MESSAGE(override val channel_id: Long, val message_id: Long): Endpoint(HttpMethod.Delete, "/channels/$channel_id/pins/$message_id")
    class LIST_GUILD_EMOJIS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/emojis")
    class GET_GUILD_EMOJI(override val guild_id: Long, val emoji_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/emojis/$emoji_id")
    class CREATE_GUILD_EMOJI(override val guild_id: Long): Endpoint(HttpMethod.Post, "/guilds/$guild_id/emojis")
    class MODIFY_GUILD_EMOJI(override val guild_id: Long, val emoji_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/emojis/$emoji_id")
    class DELETE_GUILD_EMOJI(override val guild_id: Long, val emoji_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id/emojis/$emoji_id")
    object CREATE_GUILD: Endpoint(HttpMethod.Post, "/guilds")
    class GET_GUILD(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id")
    class MODIFY_GUILD(override val guild_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id")
    class DELETE_GUILD(override val guild_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id")
    class GET_GUILD_CHANNELS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/channels")
    class CREATE_GUILD_CHANNEL(override val guild_id: Long): Endpoint(HttpMethod.Post, "/guilds/$guild_id/channels")
    class MODIFY_GUILD_CHANNEL_POSITIONS(override val guild_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/channels")
    class GET_GUILD_MEMBER(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/members/$user_id")
    class LIST_GUILD_MEMBERS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/members")
    class ADD_GUILD_MEMBER(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Put, "/guilds/$guild_id/members/$user_id")
    class MODIFY_GUILD_MEMBER(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/members/$user_id")
    class MODIFY_CURRENT_USER_NICK(override val guild_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/members/@me/nick")
    class ADD_GUILD_MEMBER_ROLE(override val guild_id: Long, val user_id: Long, val role_id: Long): Endpoint(HttpMethod.Put, "/guilds/$guild_id/members/$user_id/roles/$role_id")
    class REMOVE_GUILD_MEMBER_ROLE(override val guild_id: Long, val user_id: Long, val role_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id/members/$user_id/roles/$role_id")
    class REMOVE_GUILD_MEMBER(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id/members/$user_id")
    class GET_GUILD_BANS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/bans")
    class GET_GUILD_BAN(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/bans/$user_id")
    class CREATE_GUILD_BAN(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Put, "/guilds/$guild_id/bans/$user_id")
    class REMOVE_GUILD_BAN(override val guild_id: Long, val user_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id/bans/$user_id")
    class GET_GUILD_ROLES(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/roles")
    class CREATE_GUILD_ROLE(override val guild_id: Long): Endpoint(HttpMethod.Post, "/guilds/$guild_id/roles")
    class MODIFY_GUILD_ROLE_POSITIONS(override val guild_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/roles")
    class MODIFY_GUILD_ROLE(override val guild_id: Long, val role_id: Long): Endpoint(HttpMethod.Patch, "/guilds/$guild_id/roles/$role_id")
    class DELETE_GUILD_ROLE(override val guild_id: Long, val role_id: Long): Endpoint(HttpMethod.Delete, "/guilds/$guild_id/roles/$role_id")
    class GET_GUILD_PRUNE_COUNT(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/prune")
    class BEGIN_GUILD_PRUNE(override val guild_id: Long): Endpoint(HttpMethod.Post, "/guilds/$guild_id/prune")
    class GET_GUILD_VOICE_REGIONS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/regions")
    class GET_GUILD_INVITES(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/invites")
    class GET_GUILD_VANITY_URL(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/vanity-url")
    class GET_INVITE(val invite_code: String): Endpoint(HttpMethod.Get, "/invites/$invite_code")
    class DELETE_INVITE(val invite_code: String): Endpoint(HttpMethod.Delete, "/invites/$invite_code")
    object GET_CURRENT_USER: Endpoint(HttpMethod.Get, "/users/@me")
    class GET_USER(val user_id: Long): Endpoint(HttpMethod.Get, "/users/$user_id")
    object MODIFY_CURRENT_USER: Endpoint(HttpMethod.Patch, "/users/@me")
    object GET_CURRENT_USER_GUILDS: Endpoint(HttpMethod.Get, "/users/@me/guilds")
    class LEAVE_GUILD(override val guild_id: Long): Endpoint(HttpMethod.Delete, "/users/@me/guilds/$guild_id")
    object GET_USER_DMS: Endpoint(HttpMethod.Get, "/users/@me/channels")
    object CREATE_DM: Endpoint(HttpMethod.Post, "/users/@me/channels")
    object CREATE_GROUP_DM: Endpoint(HttpMethod.Post, "/users/@me/channels")
    object GET_USER_CONNECTIONS: Endpoint(HttpMethod.Get, "/users/@me/connections")
    object LIST_VOICE_REGIONS: Endpoint(HttpMethod.Get, "/voice/regions")
    class CREATE_WEBHOOK(override val channel_id: Long): Endpoint(HttpMethod.Post, "/channels/$channel_id/webhooks")
    class GET_CHANNEL_WEBHOOKS(override val channel_id: Long): Endpoint(HttpMethod.Get, "/channels/$channel_id/webhooks")
    class GET_GUILD_WEBHOOKS(override val guild_id: Long): Endpoint(HttpMethod.Get, "/guilds/$guild_id/webhooks")
    class GET_WEBHOOK(override val webhook_id: Long): Endpoint(HttpMethod.Get, "/webhooks/$webhook_id")
    class GET_WEBHOOK_WITH_TOKEN(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Get, " /webhooks/$webhook_id/$webhook_token")
    class MODIFY_WEBHOOK(override val webhook_id: Long): Endpoint(HttpMethod.Patch, "/webhooks/$webhook_id")
    class MODIFY_WEBHOOK_WITH_TOKEN(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Patch, "/webhooks/$webhook_id/$webhook_token")
    class DELETE_WEBHOOK(override val webhook_id: Long): Endpoint(HttpMethod.Delete, "/webhooks/$webhook_id")
    class DELETE_WEBHOOK_WITH_TOKEN(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Delete, "/webhooks/$webhook_id/$webhook_token")
    class EXECUTE_WEBHOOK(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Post, "/webhooks/$webhook_id/$webhook_token")
    class EXECUTE_SLACK_WEBHOOK(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Post, "/webhooks/$webhook_id/$webhook_token/slack")
    class EXECUTE_GITHUB_WEBHOOK(override val webhook_id: Long, val webhook_token: String): Endpoint(HttpMethod.Post, "/webhooks/$webhook_id/$webhook_token/github")
    object GET_GATEWAY: Endpoint(HttpMethod.Get, "/gateway")
    object GET_GATEWAY_BOT: Endpoint(HttpMethod.Get, "/gateway/bot")
}