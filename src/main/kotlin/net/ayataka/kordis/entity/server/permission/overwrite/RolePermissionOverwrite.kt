package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.role.Role

class RolePermissionOverwrite(
        val role: Role,
        allow: PermissionSet = PermissionSet(),
        deny: PermissionSet = PermissionSet()
) : PermissionOverwrite(role, allow, deny) {
    override fun toString(): String {
        return "RolePermissionOverwrite(role=$role, allow=$allow, deny=$deny)"
    }
}