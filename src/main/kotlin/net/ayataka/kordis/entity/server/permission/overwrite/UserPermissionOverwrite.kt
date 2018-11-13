package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.user.User

class UserPermissionOverwrite(
        val user: User,
        allow: PermissionSet = PermissionSet(),
        deny: PermissionSet = PermissionSet()
) : PermissionOverwrite(user, allow, deny) {
    override fun toString(): String {
        return "UserPermissionOverwrite(user=$user, allow=$allow, deny=$deny)"
    }
}