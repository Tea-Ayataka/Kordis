package net.ayataka.kordis

import net.ayataka.kordis.event.Event
import net.ayataka.kordis.event.Listener
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DiscordClientBuilder {
    val listeners = mutableListOf<Any>()
    val handlers = mutableListOf<Listener<Event>>()
    var token: String = ""
    var shard: Int = 0
    var maxShard: Int = 1

    fun addListener(listener: Any) = listeners.add(listener)

    inline fun <reified T : Event> addHandler(noinline action: suspend (T) -> Unit) =
            handlers.add(Listener(T::class as KClass<Event>, action as suspend (Event) -> Unit))
}