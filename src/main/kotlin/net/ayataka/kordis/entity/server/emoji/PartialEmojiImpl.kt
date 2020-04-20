package net.ayataka.kordis.entity.server.emoji

/**
 * Implementation of a [PartialEmoji]
 */
class PartialEmojiImpl(
        override val id: Long?,
        override val name: String
) : PartialEmoji