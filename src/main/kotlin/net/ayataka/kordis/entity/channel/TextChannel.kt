package net.ayataka.kordis.entity.channel

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.message.embed.EmbedBuilder
import net.ayataka.kordis.entity.message.Message

interface TextChannel : Entity {
    suspend fun send(text: String): Message {
        throw UnsupportedOperationException()
    }

    suspend fun send(block: EmbedBuilder.() -> Unit): Message {
        throw UnsupportedOperationException()
    }
}