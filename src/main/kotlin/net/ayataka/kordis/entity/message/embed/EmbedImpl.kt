package net.ayataka.kordis.entity.message.embed

import kotlinx.serialization.json.*
import net.ayataka.kordis.utils.uRgb
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EmbedImpl(
        override val title: String?,
        override val description: String?,
        override val url: String?,
        override val color: Color?,
        override val timestamp: Instant?,
        override val imageUrl: String?,
        override val thumbnailUrl: String?,
        override val footer: Footer?,
        override val author: Author?,
        override val fields: Collection<Field>
) : Embed {
    constructor(json: JsonObject) : this(
            title = json.getOrNull("title")?.content,
            description = json.getOrNull("description")?.content,
            url = json.getOrNull("url")?.content,
            color = json.getOrNull("color")?.int?.let { Color(it) },
            timestamp = json.getOrNull("timestamp")?.content
                    ?.let { Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(it)) },
            imageUrl = json.getOrNull("image")?.jsonObject?.getOrNull("url")?.content,
            thumbnailUrl = json.getOrNull("thumbnail")?.jsonObject?.getOrNull("url")?.content,
            footer = json.getOrNull("footer")?.jsonObject?.run {
                Footer(
                        getOrNull("icon_url")?.content,
                        getOrNull("text")?.content
                )
            },
            author = json.getOrNull("author")?.jsonObject?.run {
                Author(
                        getOrNull("name")?.content,
                        getOrNull("url")?.content,
                        getOrNull("icon_url")?.content
                )
            },
            fields = json.getArrayOrNull("fields")?.map {
                Field(
                        it.jsonObject["name"].content,
                        it.jsonObject["value"].content,
                        it.jsonObject.getOrNull("inline")?.boolean == true
                )
            } ?: emptyList()
    )

    override fun toJson() = json {
        "title" to title
        "description" to description
        "url" to url
        "color" to color?.uRgb()
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