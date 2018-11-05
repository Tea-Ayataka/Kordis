package net.ayataka.kordis.entity.server.permission

import net.ayataka.kordis.entity.user.User

data class UserPermissionOverwrite(
        val user: User,
        val allow: PermissionSet,
        val deny: PermissionSet
)