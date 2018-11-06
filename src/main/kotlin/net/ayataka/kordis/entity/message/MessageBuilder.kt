package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json

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

    fun build() = json {
        "content" to content
        "tts" to tts

        if (embed != null) {
            "embed" to embed!!
        }
    }
}