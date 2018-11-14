package net.ayataka.kordis.entity.server.enums

enum class UserStatus(val id: String) {
    IDLE("idle"),
    DO_NOT_DISTURB("dnd"),
    ONLINE("online"),
    OFFLINE("offline"),

    UNKNOWN("online");

    companion object {
        operator fun get(id: String) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}