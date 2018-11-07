package net.ayataka.kordis.entity.message.embed

import java.awt.Color
import java.time.Instant

class EmbedBuilder {
    var title: String? = null
        set(value) {
            if (value?.length ?: 0 > 256) {
                throw IllegalArgumentException("Embed title cannot be longer than 2000 characters")
            }
            field = value
        }

    var description: String? = null
        set(value) {
            if (value?.length ?: 0 > 2048) {
                throw IllegalArgumentException("Embed description cannot be longer than 2048 characters")
            }
            field = value
        }

    var url: String? = null
    var color: Color? = null
    var timestamp: Instant? = null
    var imageUrl: String? = null
    var thumbnailUrl: String? = null

    private var footer: Footer? = null
    private var author: Author? = null
    private val fields: MutableList<Field> = mutableListOf()

    fun footer(text: String? = null, iconUrl: String? = null) {
        if (text?.length ?: 0 > 2048) {
            throw IllegalArgumentException("Embed footer text cannot be longer than 2048 characters")
        }

        footer = Footer(text, iconUrl)
    }

    fun author(name: String? = null, url: String? = null, iconUrl: String? = null) {
        if (name?.length ?: 0 > 256) {
            throw IllegalArgumentException("Embed author name cannot be longer than 256 characters")
        }

        author = Author(name, url, iconUrl)
    }

    fun field(name: String, value: Number, inline: Boolean = false) {
        field(name, value.toString(), inline)
    }

    fun field(name: String, value: String, inline: Boolean = false) {
        if (fields.size >= 25) {
            throw IllegalArgumentException("Embed cannot have more than 25 fields")
        }

        if (name.isEmpty() || value.isEmpty()) {
            throw IllegalArgumentException("Embed field name and value cannot be empty")
        }

        if (name.length > 256) {
            throw IllegalArgumentException("Embed field name cannot be longer than 256 characters")
        }

        if (value.length > 2048) {
            throw IllegalArgumentException("Embed field value cannot be longer than 2048 characters")
        }

        fields.add(Field(name, value, inline))
    }

    fun build(): Embed {
        return EmbedImpl(title, description, url, color, timestamp, imageUrl, thumbnailUrl, footer, author, fields)
    }
}
