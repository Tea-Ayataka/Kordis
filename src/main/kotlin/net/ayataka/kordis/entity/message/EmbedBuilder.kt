package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.json
import java.awt.Color
import java.time.Instant

class EmbedBuilder {
    val content: String? = null
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

    fun footer(iconUrl: String? = null, text: String? = null) {
        footer = Footer(iconUrl, text)
    }

    fun author(name: String? = null, url: String? = null, iconUrl: String? = null) {
        author = Author(name, url, iconUrl)
    }

    fun field(name: String? = null, value: String? = null, inline: Boolean = false) {
        fields.add(Field(name, value, inline))
    }

    fun build() = json {

    }
}

data class Field(val name: String?, val value: String?, val inline: Boolean)
data class Footer(val iconUrl: String?, val text: String?)
data class Author(val name: String?, val url: String?, val iconUrl: String?)