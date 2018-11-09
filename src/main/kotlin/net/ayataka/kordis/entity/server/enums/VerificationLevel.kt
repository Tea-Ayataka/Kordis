package net.ayataka.kordis.entity.server.enums

enum class VerificationLevel(val id: Int) {
    /**
     * unrestricted
     */
    NONE(0),

    /**
     * must have verified email on account
     */
    LOW(1),

    /**
     * must be registered on Discord for longer than 5 minutes
     */
    MEDIUM(2),

    /**
     * must be a member of the server for longer than 10 minutes
     */
    HIGH(3),

    /**
     * must have a verified phone number
     */
    VERY_HIGH(4),

    UNKNOWN(-1);

    companion object {
        operator fun get(id: Int) = values().find { it != UNKNOWN && it.id == id } ?: UNKNOWN
    }
}