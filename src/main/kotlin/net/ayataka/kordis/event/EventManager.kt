package net.ayataka.kordis.event

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.utils.start
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class EventManager {
    private val dispatcher = CoroutineScope(Dispatchers.Default + CoroutineName("Event Dispatcher"))
    private val handlers = ConcurrentHashMap<KClass<out Event>, MutableList<Triple<Any?, Any, EventPriority>>>()
    private val mutex = Mutex()

    private fun getAnnotatedFunctions(instance: Any): List<KFunction<*>> {
        return instance::class.functions // Get functions
                .filter { it.annotations.any { it is EventHandler } }  // Annotation check
                .filter { it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>).isSubclassOf(Event::class) } // Parameter check
    }

    suspend fun register(handler: Any) {
        if (handler is Listener<*>) {
            mutex.withLock {
                handlers.getOrPut(handler.event) { mutableListOf() }.add(Triple(null, handler, EventPriority.NORMAL))
            }
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>
            val annotation = it.annotations.find { it is EventHandler } as EventHandler

            mutex.withLock {
                handlers.getOrPut(param) { mutableListOf() }.add(Triple(handler, it, annotation.priority))
            }
        }
    }

    suspend fun unregister(handler: Any) {
        if (handler is Listener<*>) {
            mutex.withLock {
                handlers[handler.event]?.let {
                    mutex.withLock { it.removeIf { it.second == handler } }
                }
            }
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>

            handlers[param]?.let {
                mutex.withLock { it.removeIf { it.first == handler } }
            }
        }
    }

    /**
     * Dispatch an event. This is non-blocking operation.
     */
    fun fire(event: Event) = dispatcher.start {
        handlers[event::class]?.let {
            for (item in mutex.withLock { it.sortedBy { it.third.order } }) {
                dispatcher.launch {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        (item.second as? Listener<Event>)?.action?.invoke(event)
                                ?: (item.second as? KFunction<*>)?.callSuspend(item.first, event)
                    } catch (ex: Exception) {
                        LOGGER.error("An exception occurred during invoking ${item.first?.let { it::class.qualifiedName }}::${item.second}", ex.cause
                                ?: ex)
                    }
                }
            }
        }
    }
}