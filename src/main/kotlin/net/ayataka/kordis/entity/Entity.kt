package net.ayataka.kordis.entity

import net.ayataka.kordis.DiscordClient

interface Entity {
    val client: DiscordClient
    val id: Long
}