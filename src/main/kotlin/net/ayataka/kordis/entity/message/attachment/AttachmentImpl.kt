package net.ayataka.kordis.entity.message.attachment

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.utils.getOrNull

class AttachmentImpl(client: DiscordClientImpl, json: JsonObject) : Attachment, DiscordEntity(client, json["id"].asLong) {
    override val filename = json["filename"].asString!!
    override val size = json["size"].asInt
    override val url = json["url"].asString!!
    override val proxyUrl = json["proxy_url"].asString!!
    override val height: Int? = json.getOrNull("height")?.asInt
    override val width: Int? = json.getOrNull("width")?.asInt

    override fun toString(): String {
        return "Attachment(filename='$filename', size=$size, url='$url', proxyUrl='$proxyUrl', height=$height, width=$width)"
    }
}