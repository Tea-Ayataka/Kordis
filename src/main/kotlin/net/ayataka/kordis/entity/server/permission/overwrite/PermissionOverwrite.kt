package net.ayataka.kordis.entity.server.permission.overwrite

import net.ayataka.kordis.entity.Permissionable
import net.ayataka.kordis.entity.server.permission.PermissionSet

abstract class PermissionOverwrite(
        val entity: Permissionable,
        val allow: PermissionSet,
        val deny: PermissionSet
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermissionOverwrite) return false

        if (entity != other.entity) return false
        if (allow != other.allow) return false
        if (deny != other.deny) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entity.hashCode()
        result = 31 * result + allow.hashCode()
        result = 31 * result + deny.hashCode()
        return result
    }
}