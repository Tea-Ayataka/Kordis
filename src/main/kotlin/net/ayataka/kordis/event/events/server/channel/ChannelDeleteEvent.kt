package net.ayataka.kordis.event.events.server.channel

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.event.events.server.ServerEvent

class ChannelDeleteEvent(val channel: ServerChannel) : ServerEvent {
    override val server: Server
        get() = channel.server
}