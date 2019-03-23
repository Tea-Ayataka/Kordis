package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel

class MessageEditEvent(val channel: TextChannel, val messageId: Long, val message: Message?) : MessageEvent {
    override val server: Server?
        get() = (channel as? ServerTextChannel)?.server
}