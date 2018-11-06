package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.event.Event

class MessageEditEvent(val message: Message) : Event {
    val server = message.server
}