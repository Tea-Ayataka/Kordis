package net.ayataka.kordis.entity.server.enums

enum class MessageNotificationLevel(val id: Int) {
    ALL_MESSAGES(0),
    ONLY_MENTIONS(1),

    UNKNOWN(-1);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}