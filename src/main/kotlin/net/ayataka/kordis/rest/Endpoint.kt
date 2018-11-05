package net.ayataka.kordis.rest

import io.ktor.http.HttpMethod

const val BASE = "https://discordapp.com/api/v6"
private val MAJOR_RATELIMIT_PARAMETERS = arrayOf("channel.id", "guild.id", "webhook.id")

enum class Endpoint(val method: HttpMethod, val path: String) {
    // Audit Log
    GET_GUILD_AUDIT_LOG(HttpMethod.Get, "/guilds/{guild.id}/audit-logs"),

    // Channel
    GET_CHANNEL(HttpMethod.Get, "/channels/{channel.id}"),
    MODIFY_CHANNEL_PUT(HttpMethod.Put, "/channels/{channel.id}"),
    MODIFY_CHANNEL_PATCH(HttpMethod.Patch, "/channels/{channel.id}"),
    DELETE_CHANNEL(HttpMethod.Delete, "/channels/{channel.id}"),

    EDIT_CHANNEL_PERMISSIONS(HttpMethod.Put, "/channels/{channel.id}/permissions/{overwrite.id}"),
    DELETE_CHANNEL_PERMISSION(HttpMethod.Delete, "/channels/{channel.id}/permissions/{overwrite.id}"),

    GET_CHANNEL_INVITES(HttpMethod.Get, "/channels/{channel.id}/invites"),
    CREATE_CHANNEL_INVITE(HttpMethod.Post, "/channels/{channel.id}/invites"),

    GET_CHANNEL_MESSAGE(HttpMethod.Get, "/channels/{channel.id}/messages/{message.id}"),
    GET_CHANNEL_MESSAGES(HttpMethod.Get, "/channels/{channel.id}/messages"),
    CREATE_MESSAGE(HttpMethod.Post, "/channels/{channel.id}/messages"),
    EDIT_MESSAGE(HttpMethod.Patch, "/channels/{channel.id}/messages/{message.id}"),
    DELETE_MESSAGE(HttpMethod.Delete, "/channels/{channel.id}/messages/{message.id}"),
    BULK_DELETE_MESSAGES(HttpMethod.Delete, "/channels/{channel.id}/messages/{message.id}"),

    GET_REACTIONS(HttpMethod.Get, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}"),
    CREATE_REACTION(HttpMethod.Put, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me"),
    DELETE_OWN_REACTION(HttpMethod.Delete, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me"),
    DELETE_USER_REACTION(HttpMethod.Delete, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}"),
    DELETE_ALL_REACTIONS(HttpMethod.Delete, "/channels/{channel.id}/messages/{message.id}/reactions"),

    GET_PINNED_MESSAGES(HttpMethod.Get, "/channels/{channel.id}/pins"),
    ADD_PINNED_CHANNEL_MESSAGE(HttpMethod.Put, "/channels/{channel.id}/pins/{message.id}"),
    DELETE_PINNED_CHANNEL_MESSAGE(HttpMethod.Delete, "/channels/{channel.id}/pins/{message.id}"),

    // Emoji
    LIST_GUILD_EMOJIS(HttpMethod.Get, "/guilds/{guild.id}/emojis"),
    GET_GUILD_EMOJI(HttpMethod.Get, "/guilds/{guild.id}/emojis/{emoji.id}"),
    CREATE_GUILD_EMOJI(HttpMethod.Post, "/guilds/{guild.id}/emojis"),
    MODIFY_GUILD_EMOJI(HttpMethod.Patch, "/guilds/{guild.id}/emojis/{emoji.id}"),
    DELETE_GUILD_EMOJI(HttpMethod.Delete, "/guilds/{guild.id}/emojis/{emoji.id}"),

    // Guild
    CREATE_GUILD(HttpMethod.Post, "/guilds"),
    GET_GUILD(HttpMethod.Get, "/guilds/{guild.id}"),
    MODIFY_GUILD(HttpMethod.Patch, "/guilds/{guild.id}"),
    DELETE_GUILD(HttpMethod.Delete, "/guilds/{guild.id}"),
    GET_GUILD_CHANNELS(HttpMethod.Get, "/guilds/{guild.id}/channels"),
    CREATE_GUILD_CHANNEL(HttpMethod.Post, "/guilds/{guild.id}/channels"),
    MODIFY_GUILD_CHANNEL_POSITIONS(HttpMethod.Patch, "/guilds/{guild.id}/channels"),
    GET_GUILD_MEMBER(HttpMethod.Get, "/guilds/{guild.id}/members/{user.id}"),
    LIST_GUILD_MEMBERS(HttpMethod.Get, "/guilds/{guild.id}/members"),
    ADD_GUILD_MEMBER(HttpMethod.Put, "/guilds/{guild.id}/members/{user.id}"),
    MODIFY_GUILD_MEMBER(HttpMethod.Patch, "/guilds/{guild.id}/members/{user.id}"),
    MODIFY_CURRENT_USER_NICK(HttpMethod.Patch, "/guilds/{guild.id}/members/@me/nick"),
    ADD_GUILD_MEMBER_ROLE(HttpMethod.Put, "/guilds/{guild.id}/members/{user.id}/roles/{role.id}"),
    REMOVE_GUILD_MEMBER_ROLE(HttpMethod.Delete, "/guilds/{guild.id}/members/{user.id}/roles/{role.id}"),
    REMOVE_GUILD_MEMBER(HttpMethod.Delete, "/guilds/{guild.id}/members/{user.id}"),
    GET_GUILD_BANS(HttpMethod.Get, "/guilds/{guild.id}/bans"),
    GET_GUILD_BAN(HttpMethod.Get, "/guilds/{guild.id}/bans/{user.id}"),
    CREATE_GUILD_BAN(HttpMethod.Put, "/guilds/{guild.id}/bans/{user.id}"),
    REMOVE_GUILD_BAN(HttpMethod.Delete, "/guilds/{guild.id}/bans/{user.id}"),
    GET_GUILD_ROLES(HttpMethod.Get, "/guilds/{guild.id}/roles"),
    CREATE_GUILD_ROLE(HttpMethod.Post, "/guilds/{guild.id}/roles"),
    MODIFY_GUILD_ROLE_POSITIONS(HttpMethod.Patch, "/guilds/{guild.id}/roles"),
    MODIFY_GUILD_ROLE(HttpMethod.Patch, "/guilds/{guild.id}/roles/{role.id}"),
    DELETE_GUILD_ROLE(HttpMethod.Delete, "/guilds/{guild.id}/roles/{role.id}"),
    GET_GUILD_PRUNE_COUNT(HttpMethod.Get, "/guilds/{guild.id}/prune"),
    BEGIN_GUILD_PRUNE(HttpMethod.Post, "/guilds/{guild.id}/prune"),
    GET_GUILD_VOICE_REGIONS(HttpMethod.Get, "/guilds/{guild.id}/regions"),
    GET_GUILD_INVITES(HttpMethod.Get, "/guilds/{guild.id}/invites"),
    GET_GUILD_VANITY_URL(HttpMethod.Get, "/guilds/{guild.id}/vanity-url"),

    // Invite
    GET_INVITE(HttpMethod.Get, "/invites/{invite.code}"),
    DELETE_INVITE(HttpMethod.Delete, "/invites/{invite.code}"),

    // User
    GET_CURRENT_USER(HttpMethod.Get, "/users/@me"),
    GET_USER(HttpMethod.Get, "/users/{user.id}"),
    MODIFY_CURRENT_USER(HttpMethod.Patch, "/users/@me"),
    GET_CURRENT_USER_GUILDS(HttpMethod.Get, "/users/@me/guilds"),
    LEAVE_GUILD(HttpMethod.Delete, "/users/@me/guilds/{guild.id}"),
    GET_USER_DMS(HttpMethod.Get, "/users/@me/channels"),
    CREATE_DM(HttpMethod.Post, "/users/@me/channels"),
    CREATE_GROUP_DM(HttpMethod.Post, "/users/@me/channels"),
    GET_USER_CONNECTIONS(HttpMethod.Get, "/users/@me/connections"),

    // Voice
    LIST_VOICE_REGIONS(HttpMethod.Get, "/voice/regions"),

    // Webhook
    CREATE_WEBHOOK(HttpMethod.Post, "/channels/{channel.id}/webhooks"),
    GET_CHANNEL_WEBHOOKS(HttpMethod.Get, "/channels/{channel.id}/webhooks"),
    GET_GUILD_WEBHOOKS(HttpMethod.Get, "/guilds/{guild.id}/webhooks"),
    GET_WEBHOOK(HttpMethod.Get, "/webhooks/{webhook.id}"),
    GET_WEBHOOK_WITH_TOKEN(HttpMethod.Get, "/webhooks/{webhook.id}/{webhook.token}"),
    MODIFY_WEBHOOK(HttpMethod.Patch, "/webhooks/{webhook.id}"),
    MODIFY_WEBHOOK_WITH_TOKEN(HttpMethod.Patch, "/webhooks/{webhook.id}/{webhook.token}"),
    DELETE_WEBHOOK(HttpMethod.Delete, "/webhooks/{webhook.id}"),
    DELETE_WEBHOOK_WITH_TOKEN(HttpMethod.Delete, "/webhooks/{webhook.id}/{webhook.token}"),
    EXECUTE_WEBHOOK(HttpMethod.Post, "/webhooks/{webhook.id}/{webhook.token}"),
    EXECUTE_SLACK_WEBHOOK(HttpMethod.Post, "/webhooks/{webhook.id}/{webhook.token}/slack"),
    EXECUTE_GITHUB_WEBHOOK(HttpMethod.Post, "/webhooks/{webhook.id}/{webhook.token}/github"),

    // Gateway
    GET_GATEWAY(HttpMethod.Get, "/gateway"),
    GET_GATEWAY_BOT(HttpMethod.Get, "/gateway/bot")
    ;

    fun format(args: Map<String, Any>? = null): FormattedEndPoint {
        var path = this.path
        val majorParams = mutableListOf<String>()

        args?.forEach {
            path = path.replace("{${it.key}}", it.value.toString())

            if (it.key in MAJOR_RATELIMIT_PARAMETERS) {
                majorParams.add(it.value.toString())
            }
        }

        return FormattedEndPoint(method, BASE + path, this, majorParams)
    }
}

data class FormattedEndPoint(val method: HttpMethod, val url: String, val endpoint: Endpoint, val majorParams: List<String>) {
    fun majorHash(): Int {
        var result = endpoint.hashCode()
        result = 31 * result + majorParams.hashCode()
        return result
    }
}
