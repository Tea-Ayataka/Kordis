package net.ayataka.kordis

import net.ayataka.kordis.event.Event
import net.ayataka.kordis.event.Listener
import kotlin.reflect.KClass

/**
 * Register an event handler
 */
@Suppress("UNCHECKED_CAST", "EXTENSION_SHADOWED_BY_MEMBER")
suspend inline fun <reified T : Event> DiscordClient.addHandler(noinline action: suspend (T) -> Unit) {
    (this as DiscordClientImpl).eventManager.register(Listener(T::class as KClass<Event>, action as suspend (Event) -> Unit))
}