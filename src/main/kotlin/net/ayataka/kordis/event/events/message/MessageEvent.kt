package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.event.Event

interface MessageEvent : Event {
    val server: Server?
}