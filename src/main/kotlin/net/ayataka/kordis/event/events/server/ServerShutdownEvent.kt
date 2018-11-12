package net.ayataka.kordis.event.events.server

import net.ayataka.kordis.entity.server.Server

class ServerShutdownEvent(override val server: Server) : ServerEvent