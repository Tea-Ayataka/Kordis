package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.user.PartialUser
import net.ayataka.kordis.entity.user.PartialUserImpl
import net.ayataka.kordis.entity.user.User

class UserPermissionOverwrite(
        val user: PartialUser,
        allow: PermissionSet = PermissionSet(),
        deny: PermissionSet = PermissionSet()
) : PermissionOverwrite(user, allow, deny) {
    constructor(user: User, allow: PermissionSet, deny: PermissionSet)
            : this(PartialUserImpl(user.client as DiscordClientImpl, user.id), allow, deny)

    constructor(client: DiscordClient, userId: Long, allow: PermissionSet, deny: PermissionSet)
            : this(PartialUserImpl(client as DiscordClientImpl, userId), allow, deny)

    override fun toString(): String {
        return "UserPermissionOverwrite(user=$user, allow=$allow, deny=$deny)"
    }
}