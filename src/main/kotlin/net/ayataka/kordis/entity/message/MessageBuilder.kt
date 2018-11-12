package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import net.ayataka.kordis.entity.message.embed.EmbedBuilder

class MessageBuilder {
    var content = ""
        set(value) {
            if (value.length > 2000) {
                throw IllegalArgumentException("Text length can not be longer than 2000 characters")
            }

            field = value
        }

    var tts = false
    var embed: JsonObject? = null
        private set

    fun embed(block: EmbedBuilder.() -> Unit) {
        embed = EmbedBuilder().apply(block).build().toJson()
    }

    operator fun String.unaryPlus() {
        append(this)
    }

    fun append(text: String) {
        content += text
    }

    fun appendLine(text: String) {
        content += text + "\n"
    }

    fun build() = json {
        "content" to content
        "tts" to tts

        if (embed != null) {
            "embed" to embed!!
        }
    }
}