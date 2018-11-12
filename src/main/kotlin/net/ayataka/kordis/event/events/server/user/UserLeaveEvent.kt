package net.ayataka.kordis.event.events.server.user

import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.event.events.server.ServerEvent

class UserLeaveEvent(val member: Member) : ServerEvent {
    override val server = member.server
}