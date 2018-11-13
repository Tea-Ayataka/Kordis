package net.ayataka.kordis.entity.message

/**
 * Delete the message
 */
suspend fun Message.delete() = channel.deleteMessage(id)