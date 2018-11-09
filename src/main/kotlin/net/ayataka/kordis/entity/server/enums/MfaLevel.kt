package net.ayataka.kordis.entity.server.enums

enum class MfaLevel(val id: Int) {
    NONE(0),
    ELEVATED(1),

    UNKNOWN(-1);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}