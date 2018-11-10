package net.ayataka.kordis.entity.server.enums

enum class Region(val displayName: String, val id: String) {
    AMSTERDAM("Amsterdam", "amsterdam"),
    BRAZIL("Brazil", "brazil"),
    EU_CENTRAL("Central Europe", "eu-central"),
    EU_WEST("Western Europe", "eu-west"),
    FRANKFURT("Frankfurt", "frankfurt"),
    HONG_KONG("Hong Kong", "hongkong"),
    JAPAN("Japan", "japan"),
    LONDON("London", "london"),
    RUSSIA("Russia", "russia"),
    SINGAPORE("Singapore", "singapore"),
    SOUTH_AFRICA("South Africa", "southafrica"),
    SYDNEY("Sydney", "sydney"),
    US_CENTRAL("US Central", "us-central"),
    US_EAST("US East", "us-east"),
    US_SOUTH("US South", "us-south"),
    US_WEST("US West", "us-west"),

    UNKNOWN("Unknown", "us-central");

    companion object {
        operator fun get(id: String) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}