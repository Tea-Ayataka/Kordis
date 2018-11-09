package net.ayataka.kordis.entity.message

enum class MessageType(val id: Int) {
    DEFAULT(0),
    RECIPIENT_ADD(1),
    RECIPIENT_REMOVE(2),
    CALL(3),
    CHANNEL_NAME_CHANGE(4),
    CHANNEL_ICON_CHANGE(5),
    CHANNEL_PINNED_MESSAGE(6),
    GUILD_MEMBER_JOIN(7),

    UNKNOWN(-1);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}