package net.ayataka.kordis.entity.message.embed

import com.google.gson.JsonObject
import java.awt.Color
import java.time.Instant

interface Embed {
    val title: String?
    val description: String?

    val url: String?
    val color: Color?
    val timestamp: Instant?
    val imageUrl: String?
    val thumbnailUrl: String?

    val footer: Footer?
    val author: Author?
    val fields: Collection<Field>

    fun toJson(): JsonObject
}
