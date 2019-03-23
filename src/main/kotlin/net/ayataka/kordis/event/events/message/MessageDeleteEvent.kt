package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel

class MessageDeleteEvent(val messageIds: Collection<Long>, val channel: TextChannel, override val server: Server?) : MessageEvent {
    val serverChannel = channel as? ServerTextChannel
}