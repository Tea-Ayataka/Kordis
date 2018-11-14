package net.ayataka.kordis.event.events.server.role

import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.event.events.server.ServerEvent

class RoleCreateEvent(val role: Role) : ServerEvent {
    override val server = role.server
}