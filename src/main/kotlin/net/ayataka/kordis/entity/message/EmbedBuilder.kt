package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EmbedBuilder {
    var title: String? = null
    var description: String? = null
    var url: String? = null
    var color: Color? = null
    var timestamp: Instant? = null
    var footer: Footer? = null
    var imageUrl: String? = null
    var thumbnailUrl: String? = null
    var author: Author? = null

    val fields: MutableList<Field> = mutableListOf()

    fun footer(block: Footer.() -> Unit) {
        footer = Footer(null, null).apply { block(this) }
    }

    fun author(block: Author.() -> Unit) {
        author = Author(null, null, null).apply { block(this) }
    }

    fun field(name: String? = null, value: String? = null, inline: Boolean = false) {
        fields.add(Field(name, value, inline))
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