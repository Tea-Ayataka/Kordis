package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.role.Role

class RolePermissionOverwrite(
        val role: Role,
        allow: PermissionSet,
        deny: PermissionSet
) : PermissionOverwrite(role, allow, deny)