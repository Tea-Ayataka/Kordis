package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.message.Message

class MessageReceiveEvent(val message: Message) : MessageEvent {
    override val server = message.server
}