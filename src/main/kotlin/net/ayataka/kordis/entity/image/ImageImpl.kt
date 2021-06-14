package net.ayataka.kordis.entity.image

import io.ktor.client.request.*
import net.ayataka.kordis.Kordis.HTTP_CLIENT

class ImageImpl(override val url: String) : Image {
    companion object {
        private const val IMAGE_BASE_URL = "https://cdn.discordapp.com"

        fun server(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/icons/$id/$hash.png")
        fun splash(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/splashes/$id/$hash.png")
        fun avatar(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/avatars/$id/$hash.png")
        fun defaultAvatar(discriminator: Int) = ImageImpl("$IMAGE_BASE_URL/embed/avatars/${discriminator % 5}.png")
        fun emoji(id: Long) = ImageImpl("$IMAGE_BASE_URL/emojis/$id.png")
    }

    override suspend fun bytes() = HTTP_CLIENT.get<ByteArray>(url)
}