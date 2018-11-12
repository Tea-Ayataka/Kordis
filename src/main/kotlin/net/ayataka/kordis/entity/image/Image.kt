package net.ayataka.kordis.entity.image

interface Image {
    /**
     * The url of the image
     */
    val url: String

    /**
     * Download the image
     */
    suspend fun bytes(): ByteArray
}