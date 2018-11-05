package net.ayataka.kordis.entity.server.permission

import net.ayataka.kordis.entity.server.Role

data class RolePermissionOverwrite(
        val role: Role,
        val allow: PermissionSet,
        val deny: PermissionSet
)