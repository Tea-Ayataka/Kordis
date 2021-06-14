package net.ayataka.kordis.entity.channel

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.message.MessageBuilder

interface TextChannel : Entity {
    /**
     * Send a message to the channel
     */
    suspend fun send(text: String): Message

    /**
     * Send an embedded message to the channel
     */
    suspend fun send(block: MessageBuilder.() -> Unit): Message

    /**
     * Get a message by its id
     */
    suspend fun getMessage(messageId: Long): Message?

    /**
     * Get message history
     *
     * @param limit 1-100
     */
    suspend fun getMessages(limit: Int = 100): Collection<Message>

    /**
     * Delete a message by its id
     */
    suspend fun deleteMessage(messageId: Long)
}