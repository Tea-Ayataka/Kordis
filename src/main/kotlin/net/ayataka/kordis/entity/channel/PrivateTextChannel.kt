package net.ayataka.kordis.entity.channel

import net.ayataka.kordis.entity.user.User

interface PrivateTextChannel : TextChannel {
    /**
     * The recipients of the DM
     */
    val recipients: Collection<User>

    /**
     * The owner of the DM
     */
    val owner: User?
}