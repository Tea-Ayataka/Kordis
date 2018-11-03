package net.ayataka.kordis.entity.server.enums

enum class VerificationLevel {
    /**
     * unrestricted
     */
    NONE,

    /**
     * must have verified email on account
     */
    LOW,

    /**
     * must be registered on Discord for longer than 5 minutes
     */
    MEDIUM,

    /**
     * must be a member of the server for longer than 10 minutes
     */
    HIGH,

    /**
     * must have a verified phone number
     */
    VERY_HIGH
}