package net.ayataka.kordis.event.events

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.event.Event

class ServerReadyEvent(val server: Server) : Event