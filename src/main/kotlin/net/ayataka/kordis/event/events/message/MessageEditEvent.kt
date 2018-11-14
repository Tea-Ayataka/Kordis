package net.ayataka.kordis.event.events.message

import net.ayataka.kordis.entity.channel.TextChannel
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.event.Event

class MessageEditEvent(val channel: TextChannel, val messageId: Long, val message: Message?) : Event