package net.ayataka.kordis.entity.server.updater

import net.ayataka.kordis.entity.server.Role

class RoleUpdater(val role: Role) {
    var name = role.name
    var position = role.position
    var color = role.color
    var hoist = role.hoist
    var permissions = role.permissions
    var mentionable = role.mentionable
}