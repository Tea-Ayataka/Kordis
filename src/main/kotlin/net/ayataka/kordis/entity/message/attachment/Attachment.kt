package net.ayataka.kordis.entity.message.attachment

import net.ayataka.kordis.entity.Entity

interface Attachment : Entity {
    /**
     * The name of the file attached
     */
    val filename: String

    /**
     * The size of the file in bytes
     */
    val size: Int

    /**
     * The source url of the file
     */
    val url: String

    /**
     * The proxied url of the file
     */
    val proxyUrl: String

    /**
     * The height of the file (if it's image)
     */
    val height: Int?

    /**
     * The width of the file (if it's image)
     */
    val width: Int?

    /**
     * Whether the file is image
     */
    val isImage: Boolean
        get() = height != null || width != null
}