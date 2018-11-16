package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.event.Event

class MessageEditEvent(val channel: TextChannel, val messageId: Long, val message: Message?) : Event {
    val server: Server?
        get() = (channel as? ServerTextChannel)?.server
}