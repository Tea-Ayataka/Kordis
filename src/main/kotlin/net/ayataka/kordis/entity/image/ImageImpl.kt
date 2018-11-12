package net.ayataka.kordis.entity.image

import net.ayataka.kordis.Kordis.HTTP_CLIENT
import net.ayataka.kordis.utils.executeAsync
import okhttp3.Request

class ImageImpl(override val url: String) : Image {
    companion object {
        private const val IMAGE_BASE_URL = "https://cdn.discordapp.com"

        fun server(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/icons/$id/$hash.png")
        fun splash(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/splashes/$id/$hash.png")
        fun avatar(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/avatars/$id/$hash.png")
        fun emoji(id: Long) = ImageImpl("$IMAGE_BASE_URL/emojis/$id.png")
    }

    override suspend fun bytes() =
            HTTP_CLIENT.newCall(Request.Builder().url(url).build()).executeAsync().body()!!.bytes()!!
}