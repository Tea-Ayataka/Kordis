package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.event.Event

class MessageDeleteEvent(val messageIds: Collection<Long>, val channel: TextChannel, val server: Server?) : Event {
    val serverChannel = channel as? ServerTextChannel
}