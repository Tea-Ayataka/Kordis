package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

    fun footer(block: Footer.() -> Unit) {
        val footer = Footer(null, null).apply { block(this) }
        if (footer.text?.length ?: 0 > 2048) {
            throw IllegalArgumentException("Embed footer text cannot be longer than 2048 characters")
        }

        this.footer = footer
    }

    fun author(block: Author.() -> Unit) {
        val author = Author(null, null, null).apply { block(this) }
        if (author.name?.length ?: 0 > 256) {
            throw IllegalArgumentException("Embed author name cannot be longer than 256 characters")
        }

        this.author = author
    }

    fun field(block: Field.() -> Unit) {
        if (fields.size >= 25) {
            throw IllegalArgumentException("Embed cannot have more than 25 fields")
        }

        val field = Field(null, null, false).apply { block(this) }
        if (field.name?.length ?: 0 > 256) {
            throw IllegalArgumentException("Embed field name cannot be longer than 256 characters")
        }

        if (field.value?.length ?: 0 > 2048) {
            throw IllegalArgumentException("Embed field value cannot be longer than 2048 characters")
        }

        fields.add(field)
    }

    fun build() = json {
        "title" to title
        "description" to description
        "url" to url
        "color" to color?.run { ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0) }
        "timestamp" to timestamp?.run { DateTimeFormatter.ISO_DATE_TIME.format(this.atOffset(ZoneOffset.UTC)) }
        "footer" to json {
            "icon_url" to footer?.iconUrl
            "text" to footer?.text
        }
        "thumbnail" to json {
            "url" to thumbnailUrl
        }
        "image" to json {
            "url" to imageUrl
        }
        "author" to json {
            "name" to author?.name
            "url" to author?.url
            "icon_url" to author?.iconUrl
        }
        "fields" to jsonArray {
            fields.forEach {
                +json {
                    "name" to it.name
                    "value" to it.value
                    "inline" to it.inline
                }
            }
        }
    }
}

data class Field(var name: String?, var value: String?, var inline: Boolean)
data class Footer(var iconUrl: String?, var text: String?)
data class Author(var name: String?, var url: String?, var iconUrl: String?)