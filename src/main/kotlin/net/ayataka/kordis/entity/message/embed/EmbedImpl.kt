package net.ayataka.kordis.entity.message.embed

import com.google.gson.JsonObject
import net.ayataka.kordis.utils.getArrayOrNull
import net.ayataka.kordis.utils.getOrNull
import net.ayataka.kordis.utils.json
import net.ayataka.kordis.utils.jsonArray
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
            title = json.getOrNull("title")?.asString,
            description = json.getOrNull("description")?.asString,
            url = json.getOrNull("url")?.asString,
            color = json.getOrNull("color")?.asInt?.let { Color(it) },
            timestamp = json.getOrNull("timestamp")?.asString
                    ?.let { Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(it)) },
            imageUrl = json.getOrNull("image")?.asJsonObject?.getOrNull("url")?.asString,
            thumbnailUrl = json.getOrNull("thumbnail")?.asJsonObject?.getOrNull("url")?.asString,
            footer = json.getOrNull("footer")?.asJsonObject?.run {
                Footer(
                        getOrNull("icon_url")?.asString,
                        getOrNull("text")?.asString
                )
            },
            author = json.getOrNull("author")?.asJsonObject?.run {
                Author(
                        getOrNull("name")?.asString,
                        getOrNull("url")?.asString,
                        getOrNull("icon_url")?.asString
                )
            },
            fields = json.getArrayOrNull("fields")?.map {
                Field(
                        it.asJsonObject["name"].asString,
                        it.asJsonObject["value"].asString,
                        it.asJsonObject.getOrNull("inline")?.asBoolean == true
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