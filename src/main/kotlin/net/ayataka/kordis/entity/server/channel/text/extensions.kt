package net.ayataka.kordis.entity.server.channel.text

import net.ayataka.kordis.entity.message.Message
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Bulk delete messages
 */
suspend fun ServerTextChannel.deleteMessages(messages: Collection<Message>) {
    if (messages.isEmpty()) {
        return
    }

    if (messages.size == 1) {
        deleteMessage(messages.first().id)
        return
    }

    deleteMessages(
            messages.filter { it.timestamp.isAfter(Instant.now().minus(14, ChronoUnit.DAYS)) }
                    .map { it.id }
    )
}