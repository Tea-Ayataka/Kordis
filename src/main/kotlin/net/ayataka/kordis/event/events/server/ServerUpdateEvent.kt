package net.ayataka.kordis.event.events.server

import net.ayataka.kordis.entity.server.Server

class ServerUpdateEvent(override val server: Server) : ServerEvent