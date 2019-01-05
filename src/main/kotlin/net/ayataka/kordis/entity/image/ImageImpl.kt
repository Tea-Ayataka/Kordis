package net.ayataka.kordis.entity.image

import kotlinx.coroutines.future.await
import net.ayataka.kordis.Kordis.HTTP_CLIENT
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ImageImpl(override val url: String) : Image {
    companion object {
        private const val IMAGE_BASE_URL = "https://cdn.discordapp.com"

        fun server(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/icons/$id/$hash.png")
        fun splash(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/splashes/$id/$hash.png")
        fun avatar(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/avatars/$id/$hash.png")
        fun defaultAvatar(discriminator: Int) = ImageImpl("$IMAGE_BASE_URL/embed/avatars/${discriminator % 5}.png")
        fun emoji(id: Long) = ImageImpl("$IMAGE_BASE_URL/emojis/$id.png")
    }

    override suspend fun bytes() =
            HTTP_CLIENT.sendAsync(HttpRequest.newBuilder(URI(url)).build(), HttpResponse.BodyHandlers.ofByteArray()).await().body()!!
}