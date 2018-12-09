package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.Permissionable
import net.ayataka.kordis.entity.channel.PrivateTextChannel
import net.ayataka.kordis.entity.image.Image
import net.ayataka.kordis.entity.server.Server

interface User : Mentionable, Nameable, Permissionable, Entity {
    /**
     * Whether the user is bot
     */
    val bot: Boolean

    /**
     * The avatar image of the user
     */
    val avatar: Image

    /**
     * The discriminator of the user
     */
    val discriminator: String

    /**
     * The mention tag of the user
     */
    override val mention: String
        get() = "<@$id>"

    /**
     * The full tag of the user
     */
    val tag: String
        get() = "$name#$discriminator"

    /**
     * Ban the user from a server
     */
    suspend fun ban(server: Server, deleteMessageDays: Int = 0, reason: String? = null) = server.ban(this, deleteMessageDays, reason)

    /**
     * Unban the user from a server
     */
    suspend fun unban(server: Server) = server.unban(this)

    /**
     * Get a member object by the user
     */
    fun toMember(server: Server) = server.members.find(id)

    /**
     * Get the private channel for the user
     */
    suspend fun getPrivateChannel(): PrivateTextChannel
}
