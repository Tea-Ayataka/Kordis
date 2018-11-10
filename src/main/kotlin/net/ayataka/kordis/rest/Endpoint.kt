package net.ayataka.kordis.rest


const val BASE = "https://discordapp.com/api/v6"
private val MAJOR_RATELIMIT_PARAMETERS = arrayOf("channel.id", "guild.id", "webhook.id")

enum class Endpoint(val method: HttpMethod, val path: String) {
    // Audit Log
    GET_GUILD_AUDIT_LOG(HttpMethod.GET, "/guilds/{guild.id}/audit-logs"),

    // Channel
    GET_CHANNEL(HttpMethod.GET, "/channels/{channel.id}"),
    MODIFY_CHANNEL_PUT(HttpMethod.PUT, "/channels/{channel.id}"),
    MODIFY_CHANNEL_PATCH(HttpMethod.PATCH, "/channels/{channel.id}"),
    DELETE_CHANNEL(HttpMethod.DELETE, "/channels/{channel.id}"),

    EDIT_CHANNEL_PERMISSIONS(HttpMethod.PUT, "/channels/{channel.id}/permissions/{overwrite.id}"),
    DELETE_CHANNEL_PERMISSION(HttpMethod.DELETE, "/channels/{channel.id}/permissions/{overwrite.id}"),

    GET_CHANNEL_INVITES(HttpMethod.GET, "/channels/{channel.id}/invites"),
    CREATE_CHANNEL_INVITE(HttpMethod.POST, "/channels/{channel.id}/invites"),

    GET_CHANNEL_MESSAGE(HttpMethod.GET, "/channels/{channel.id}/messages/{message.id}"),
    GET_CHANNEL_MESSAGES(HttpMethod.GET, "/channels/{channel.id}/messages"),
    CREATE_MESSAGE(HttpMethod.POST, "/channels/{channel.id}/messages"),
    EDIT_MESSAGE(HttpMethod.PATCH, "/channels/{channel.id}/messages/{message.id}"),
    DELETE_MESSAGE(HttpMethod.DELETE, "/channels/{channel.id}/messages/{message.id}"),
    BULK_DELETE_MESSAGES(HttpMethod.DELETE, "/channels/{channel.id}/messages/{message.id}"),

    GET_REACTIONS(HttpMethod.GET, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}"),
    CREATE_REACTION(HttpMethod.PUT, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me"),
    DELETE_OWN_REACTION(HttpMethod.DELETE, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me"),
    DELETE_USER_REACTION(HttpMethod.DELETE, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}"),
    DELETE_ALL_REACTIONS(HttpMethod.DELETE, "/channels/{channel.id}/messages/{message.id}/reactions"),

    GET_PINNED_MESSAGES(HttpMethod.GET, "/channels/{channel.id}/pins"),
    ADD_PINNED_CHANNEL_MESSAGE(HttpMethod.PUT, "/channels/{channel.id}/pins/{message.id}"),
    DELETE_PINNED_CHANNEL_MESSAGE(HttpMethod.DELETE, "/channels/{channel.id}/pins/{message.id}"),

    // Emoji
    LIST_GUILD_EMOJIS(HttpMethod.GET, "/guilds/{guild.id}/emojis"),
    GET_GUILD_EMOJI(HttpMethod.GET, "/guilds/{guild.id}/emojis/{emoji.id}"),
    CREATE_GUILD_EMOJI(HttpMethod.POST, "/guilds/{guild.id}/emojis"),
    MODIFY_GUILD_EMOJI(HttpMethod.PATCH, "/guilds/{guild.id}/emojis/{emoji.id}"),
    DELETE_GUILD_EMOJI(HttpMethod.DELETE, "/guilds/{guild.id}/emojis/{emoji.id}"),

    // Guild
    CREATE_GUILD(HttpMethod.POST, "/guilds"),
    GET_GUILD(HttpMethod.GET, "/guilds/{guild.id}"),
    MODIFY_GUILD(HttpMethod.PATCH, "/guilds/{guild.id}"),
    DELETE_GUILD(HttpMethod.DELETE, "/guilds/{guild.id}"),
    GET_GUILD_CHANNELS(HttpMethod.GET, "/guilds/{guild.id}/channels"),
    CREATE_GUILD_CHANNEL(HttpMethod.POST, "/guilds/{guild.id}/channels"),
    MODIFY_GUILD_CHANNEL_POSITIONS(HttpMethod.PATCH, "/guilds/{guild.id}/channels"),
    GET_GUILD_MEMBER(HttpMethod.GET, "/guilds/{guild.id}/members/{user.id}"),
    LIST_GUILD_MEMBERS(HttpMethod.GET, "/guilds/{guild.id}/members"),
    ADD_GUILD_MEMBER(HttpMethod.PUT, "/guilds/{guild.id}/members/{user.id}"),
    MODIFY_GUILD_MEMBER(HttpMethod.PATCH, "/guilds/{guild.id}/members/{user.id}"),
    MODIFY_CURRENT_USER_NICK(HttpMethod.PATCH, "/guilds/{guild.id}/members/@me/nick"),
    ADD_GUILD_MEMBER_ROLE(HttpMethod.PUT, "/guilds/{guild.id}/members/{user.id}/roles/{role.id}"),
    REMOVE_GUILD_MEMBER_ROLE(HttpMethod.DELETE, "/guilds/{guild.id}/members/{user.id}/roles/{role.id}"),
    REMOVE_GUILD_MEMBER(HttpMethod.DELETE, "/guilds/{guild.id}/members/{user.id}"),
    GET_GUILD_BANS(HttpMethod.GET, "/guilds/{guild.id}/bans"),
    GET_GUILD_BAN(HttpMethod.GET, "/guilds/{guild.id}/bans/{user.id}"),
    CREATE_GUILD_BAN(HttpMethod.PUT, "/guilds/{guild.id}/bans/{user.id}"),
    REMOVE_GUILD_BAN(HttpMethod.DELETE, "/guilds/{guild.id}/bans/{user.id}"),
    GET_GUILD_ROLES(HttpMethod.GET, "/guilds/{guild.id}/roles"),
    CREATE_GUILD_ROLE(HttpMethod.POST, "/guilds/{guild.id}/roles"),
    MODIFY_GUILD_ROLE_POSITIONS(HttpMethod.PATCH, "/guilds/{guild.id}/roles"),
    MODIFY_GUILD_ROLE(HttpMethod.PATCH, "/guilds/{guild.id}/roles/{role.id}"),
    DELETE_GUILD_ROLE(HttpMethod.DELETE, "/guilds/{guild.id}/roles/{role.id}"),
    GET_GUILD_PRUNE_COUNT(HttpMethod.GET, "/guilds/{guild.id}/prune"),
    BEGIN_GUILD_PRUNE(HttpMethod.POST, "/guilds/{guild.id}/prune"),
    GET_GUILD_VOICE_REGIONS(HttpMethod.GET, "/guilds/{guild.id}/regions"),
    GET_GUILD_INVITES(HttpMethod.GET, "/guilds/{guild.id}/invites"),
    GET_GUILD_VANITY_URL(HttpMethod.GET, "/guilds/{guild.id}/vanity-url"),

    // Invite
    GET_INVITE(HttpMethod.GET, "/invites/{invite.code}"),
    DELETE_INVITE(HttpMethod.DELETE, "/invites/{invite.code}"),

    // User
    GET_CURRENT_USER(HttpMethod.GET, "/users/@me"),
    GET_USER(HttpMethod.GET, "/users/{user.id}"),
    MODIFY_CURRENT_USER(HttpMethod.PATCH, "/users/@me"),
    GET_CURRENT_USER_GUILDS(HttpMethod.GET, "/users/@me/guilds"),
    LEAVE_GUILD(HttpMethod.DELETE, "/users/@me/guilds/{guild.id}"),
    GET_USER_DMS(HttpMethod.GET, "/users/@me/channels"),
    CREATE_DM(HttpMethod.POST, "/users/@me/channels"),
    CREATE_GROUP_DM(HttpMethod.POST, "/users/@me/channels"),
    GET_USER_CONNECTIONS(HttpMethod.GET, "/users/@me/connections"),

    // Voice
    LIST_VOICE_REGIONS(HttpMethod.GET, "/voice/regions"),

    // Webhook
    CREATE_WEBHOOK(HttpMethod.POST, "/channels/{channel.id}/webhooks"),
    GET_CHANNEL_WEBHOOKS(HttpMethod.GET, "/channels/{channel.id}/webhooks"),
    GET_GUILD_WEBHOOKS(HttpMethod.GET, "/guilds/{guild.id}/webhooks"),
    GET_WEBHOOK(HttpMethod.GET, "/webhooks/{webhook.id}"),
    GET_WEBHOOK_WITH_TOKEN(HttpMethod.GET, "/webhooks/{webhook.id}/{webhook.token}"),
    MODIFY_WEBHOOK(HttpMethod.PATCH, "/webhooks/{webhook.id}"),
    MODIFY_WEBHOOK_WITH_TOKEN(HttpMethod.PATCH, "/webhooks/{webhook.id}/{webhook.token}"),
    DELETE_WEBHOOK(HttpMethod.DELETE, "/webhooks/{webhook.id}"),
    DELETE_WEBHOOK_WITH_TOKEN(HttpMethod.DELETE, "/webhooks/{webhook.id}/{webhook.token}"),
    EXECUTE_WEBHOOK(HttpMethod.POST, "/webhooks/{webhook.id}/{webhook.token}"),
    EXECUTE_SLACK_WEBHOOK(HttpMethod.POST, "/webhooks/{webhook.id}/{webhook.token}/slack"),
    EXECUTE_GITHUB_WEBHOOK(HttpMethod.POST, "/webhooks/{webhook.id}/{webhook.token}/github"),

    // Gateway
    GET_GATEWAY(HttpMethod.GET, "/gateway"),
    GET_GATEWAY_BOT(HttpMethod.GET, "/gateway/bot")
    ;

    fun format(vararg args: Pair<String, Any>): FormattedEndPoint {
        var path = this.path
        val majorParams = mutableListOf<String>()

        args.forEach {
            path = path.replace("{${it.first}}", it.second.toString())

            if (it.first in MAJOR_RATELIMIT_PARAMETERS) {
                majorParams.add(it.second.toString())
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
