package net.ayataka.kordis.event.events.server

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.event.Event

interface ServerEvent : Event {
    val server: Server
}