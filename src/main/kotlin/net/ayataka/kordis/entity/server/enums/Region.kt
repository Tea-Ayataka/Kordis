package net.ayataka.kordis.entity.server.enums

enum class Region(val displayName: String, val id: String) {
    US_EAST("US East", "us-east"),
    US_WEST("US West", "us-west"),
    US_CENTRAL("US Central", "us-central"),

    UNKNOWN("Unknown", "us-central");

    companion object {
        operator fun get(id: String) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}