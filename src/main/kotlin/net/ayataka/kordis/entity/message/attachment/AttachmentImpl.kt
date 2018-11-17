package net.ayataka.kordis.entity.message.attachment

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity

class AttachmentImpl(client: DiscordClientImpl, json: JsonObject) : Attachment, DiscordEntity(client, json["id"].long) {
    override val filename = json["filename"].content
    override val size = json["size"].int
    override val url = json["url"].content
    override val proxyUrl = json["proxy_url"].content
    override val height: Int? = json.getOrNull("height")?.int
    override val width: Int? = json.getOrNull("width")?.int

    override fun toString(): String {
        return "Attachment(filename='$filename', size=$size, url='$url', proxyUrl='$proxyUrl', height=$height, width=$width)"
    }
}