package net.ayataka.kordis.entity.server.emoji

/**
 * Represents a partial emoji, used in reactions.
 *
 * **See Also**
 *
 * [Emoji object](https://discordapp.com/developers/docs/resources/emoji#emoji-object)
 *
 * [Reaction Standard Emoji](https://discordapp.com/developers/docs/resources/emoji#emoji-object-gateway-reaction-standard-emoji-example)
 */
interface PartialEmoji {
    val id: Long?
    val name: String
}
