package net.ayataka.kordis.event.events.server.channel

import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.event.events.server.ServerEvent

class ChannelCreateEvent(val channel: ServerChannel) : ServerEvent {
    override val server
        get() = channel.server
}