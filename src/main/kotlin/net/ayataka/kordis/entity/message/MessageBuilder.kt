package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json

class MessageBuilder {
    var content = ""
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