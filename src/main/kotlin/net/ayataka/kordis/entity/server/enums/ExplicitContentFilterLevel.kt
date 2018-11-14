package net.ayataka.kordis.entity.server.enums

enum class ExplicitContentFilterLevel(val id: Int) {
    DISABLED(0),
    MEMBERS_WITHOUT_ROLES(1),
    ALL_MEMBERS(2),

    UNKNOWN(-1);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}