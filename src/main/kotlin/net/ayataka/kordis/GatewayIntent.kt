package net.ayataka.kordis

enum class GatewayIntent(val flag: Int) {
    /**
    - GUILD_CREATE
    - GUILD_DELETE
    - GUILD_ROLE_CREATE
    - GUILD_ROLE_UPDATE
    - GUILD_ROLE_DELETE
    - CHANNEL_CREATE
    - CHANNEL_UPDATE
    - CHANNEL_DELETE
    - CHANNEL_PINS_UPDATE
     */
    GUILDS(1 shl 0),

    /**
    - GUILD_MEMBER_ADD
    - GUILD_MEMBER_UPDATE
    - GUILD_MEMBER_REMOVE
     */
    GUILD_MEMBERS(1 shl 1),

    /**
    - GUILD_BAN_ADD
    - GUILD_BAN_REMOVE
     */
    GUILD_BANS(1 shl 2),

    /**
    - GUILD_EMOJIS_UPDATE
     */
    GUILD_EMOJIS(1 shl 3),

    /**
    - GUILD_INTEGRATIONS_UPDATE
     */
    GUILD_INTEGRATIONS(1 shl 4),

    /**
    - WEBHOOKS_UPDATE
     */
    GUILD_WEBHOOKS(1 shl 5),

    /**
    - INVITE_CREATE
    - INVITE_DELETE
     */
    GUILD_INVITES(1 shl 6),

    /**
    - VOICE_STATE_UPDATE
     */
    GUILD_VOICE_STATES(1 shl 7),

    /**
    - PRESENCE_UPDATE
     */
    GUILD_PRESENCES(1 shl 8),

    /**
    - MESSAGE_CREATE
    - MESSAGE_UPDATE
    - MESSAGE_DELETE
     */
    GUILD_MESSAGES(1 shl 9),

    /**
    - MESSAGE_REACTION_ADD
    - MESSAGE_REACTION_REMOVE
    - MESSAGE_REACTION_REMOVE_ALL
    - MESSAGE_REACTION_REMOVE_EMOJI
     */
    GUILD_MESSAGE_REACTIONS(1 shl 10),

    /**
    - TYPING_START
     */
    GUILD_MESSAGE_TYPING(1 shl 11),

    /**
    - CHANNEL_CREATE
    - MESSAGE_CREATE
    - MESSAGE_UPDATE
    - MESSAGE_DELETE
    - CHANNEL_PINS_UPDATE
     */
    DIRECT_MESSAGES(1 shl 12),

    /**
    - MESSAGE_REACTION_ADD
    - MESSAGE_REACTION_REMOVE
    - MESSAGE_REACTION_REMOVE_ALL
    - MESSAGE_REACTION_REMOVE_EMOJI
     */
    DIRECT_MESSAGE_REACTIONS(1 shl 13),

    /**
    - TYPING_START
     */
    DIRECT_MESSAGE_TYPING(1 shl 14),
}