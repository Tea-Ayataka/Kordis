package net.ayataka.kordis.event

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ayataka.kordis.Kordis.LOGGER
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class EventManager {
    private val dispatcher = CoroutineScope(Dispatchers.Default + CoroutineName("Event Dispatcher"))
    private val queue = LinkedBlockingQueue<suspend CoroutineScope.() -> Unit>()
    private val handlers = ConcurrentHashMap<KClass<out Event>, MutableList<Pair<Any?, Any>>>()

    init {
        Thread({
            while (true) {
                dispatcher.launch(block = queue.take())
            }
        }, "Event Dispatcher").start()
    }

    private fun getAnnotatedFunctions(instance: Any): List<KFunction<*>> {
        return instance::class.functions // Get functions
                .filter { it.annotations.any { it is EventHandler } }  // Annotation check
                .filter { it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>).isSubclassOf(Event::class) } // Parameter check
    }

    fun register(handler: Any) = synchronized(handlers) {
        if (handler is Listener<*>) {
            handlers.getOrPut(handler.event) { mutableListOf() }.add(Pair(null, handler))
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>
            handlers.getOrPut(param) { mutableListOf() }.add(Pair(handler, it))
        }
    }

    fun unregister(handler: Any) = synchronized(handlers) {
        if (handler is Listener<*>) {
            handlers[handler.event]?.let {
                it.removeIf { it.second == handler }
            }
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>

            handlers[param]?.let {
                it.removeIf { it.first == handler }
            }
        }
    }

    /**
     * Dispatch an event.
     */
    fun fire(event: Event) {
        val handlersOfTheEvent = synchronized(handlers) { handlers[event::class] } ?: return
        for (item in handlersOfTheEvent) {
            queue.put {
                try {
                    @Suppress("UNCHECKED_CAST")
                    (item.second as? Listener<Event>)?.action?.invoke(event)
                            ?: (item.second as? KFunction<*>)?.callSuspend(item.first, event)
                } catch (ex: Exception) {
                    LOGGER.error("An exception occurred during invoking ${item.second}", ex.cause ?: ex)
                }
            }
        }
    }
}