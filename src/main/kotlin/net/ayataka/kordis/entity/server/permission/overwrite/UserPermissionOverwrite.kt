package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.user.User

class UserPermissionOverwrite(
        val user: User,
        allow: PermissionSet,
        deny: PermissionSet
) : PermissionOverwrite(user, allow, deny)