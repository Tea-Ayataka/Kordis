package net.ayataka.kordis.event.events.server.user

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User
import net.ayataka.kordis.event.events.server.ServerEvent

class UserUnbanEvent(override val server: Server, val user: User) : ServerEvent