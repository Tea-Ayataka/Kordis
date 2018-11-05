package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json

class MessageBuilder {
    private var content = ""
    private var tts = false
    private var embed: JsonObject? = null

    fun content(value: String) {
        content = value
    }

    fun embed(value: JsonObject) {
        embed = value
    }

    fun tts(value: Boolean) {
        tts = value
    }

    fun build() = json {
        "content" to content
        "tts" to tts

        if (embed != null) {
            "embed" to embed!!
        }
    }
}