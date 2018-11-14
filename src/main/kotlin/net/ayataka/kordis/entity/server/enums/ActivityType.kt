package net.ayataka.kordis.entity.server.enums

enum class ActivityType(val id: Int) {
    PLAYING(0),
    STREAMING(1),
    LISTENING(2),
    WATCHING(3),

    UNKNOWN(0);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}