package net.ayataka.kordis.event

import net.ayataka.kordis.entity.server.Server

interface ServerEvent : Event {
    val server: Server
}