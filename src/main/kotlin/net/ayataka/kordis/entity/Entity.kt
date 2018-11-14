package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClient
import java.time.Instant

interface Entity {
    /**
     * The DiscordClient instance this entity was created by
     */
    val client: DiscordClient

    /**
     * The snowflake id of this entity
     */
    val id: Long

    /**
     * The creation timestamp of this entity
     *
     * This is calculated from its snowflake id
     * See https://discordapp.com/developers/docs/reference#snowflake-ids
     */
    val timestamp: Instant
        get() = Instant.ofEpochMilli((id ushr 22) + 1420070400000L)
}