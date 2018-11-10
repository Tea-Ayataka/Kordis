package net.ayataka.kordis.entity.server.role

class RoleBuilder(role: Role? = null) {
    var name = role?.name
    var position = role?.position
    var color = role?.color
    var hoist = role?.hoist
    var permissions = role?.permissions
    var mentionable = role?.mentionable
}