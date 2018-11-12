package net.ayataka.kordis.entity.image

class ImageImpl(override val url: String) : Image {
    companion object {
        private const val IMAGE_BASE_URL = "https://cdn.discordapp.com"

        fun server(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/icons/$id/$hash.png")
        fun splash(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/splashes/$id/$hash.png")
        fun avatar(id: Long, hash: String) = ImageImpl("$IMAGE_BASE_URL/avatars/$id/$hash.png")
        fun emoji(id: Long) = ImageImpl("$IMAGE_BASE_URL/emojis/$id.png")
    }
}