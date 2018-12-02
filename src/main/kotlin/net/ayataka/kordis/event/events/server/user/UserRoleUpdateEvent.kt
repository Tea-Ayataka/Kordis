package net.ayataka.kordis.event.events.server.user

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.event.events.server.ServerEvent

class UserRoleUpdateEvent(val member: Member, val before: Collection<Role>) : ServerEvent {
    override val server: Server
        get() = member.server
}