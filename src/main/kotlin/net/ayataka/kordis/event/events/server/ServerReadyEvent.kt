package net.ayataka.kordis.event.events.server

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.event.Event

class ServerReadyEvent(override val server: Server) : ServerEvent