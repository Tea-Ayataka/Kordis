package net.ayataka.kordis.entity.server.enums

enum class ChannelType(val id: Int) {
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4);
}