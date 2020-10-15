package net.ayataka.kordis.entity.user

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Permissionable
import net.ayataka.kordis.entity.server.Server

interface PartialUser: Mentionable, Permissionable, Entity {
    /**
     * The mention tag of the user
     */
    override val mention: String
        get() = "<@$id>"

    suspend fun asUser(): User? = client.getUser(id)
    suspend fun asMember(server: Server) = server.findMember(id)
}