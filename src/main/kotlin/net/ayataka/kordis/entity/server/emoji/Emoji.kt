package net.ayataka.kordis.entity.server.emoji

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.server.Server

interface Emoji : Nameable, Entity {
    /**
     * The parent server of the emoji
     */
    val server: Server

    /**
     * The image of the emoji
     */
    val image: Image

    /**
     * Delete the emoji
     */
    suspend fun delete()

    /**
     * Edit the name of the emoji
     */
    suspend fun edit(name: String)
}