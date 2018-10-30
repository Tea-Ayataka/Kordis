package net.ayataka.kordis.event

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.utils.start
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class EventManager {
    private val dispatcher = CoroutineScope(Dispatchers.Default + CoroutineName("Event Dispatcher"))
    private val handlers = ConcurrentHashMap<KClass<out Event>, MutableList<Triple<Any, KFunction<*>, EventPriority>>>()
    private val mutex = Mutex()

    private fun getAnnotatedFunctions(instance: Any): List<KFunction<*>> {
        return instance::class.functions // Get functions
                .filter { it.annotations.any { it is EventListener } }  // Annotation check
                .filter { it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>).isSubclassOf(Event::class) } // Parameter check
    }

    suspend fun register(handler: Any) {
        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>
            val annotation = it.annotations.find { it is EventListener } as EventListener

            mutex.withLock {
                if (handlers[param] == null) {
                    handlers[param] = mutableListOf()
                }

                handlers[param]!!.add(Triple(handler, it, annotation.priority))
            }
        }
    }

    suspend fun unregister(handler: Any) {
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
    fun fire(event: Event) = GlobalScope.start {
        handlers[event::class]?.let {
            for (item in mutex.withLock { it.sortedBy { it.third.order } }) {
                dispatcher.launch {
                    try {
                        item.second.callSuspend(item.first, event)
                    } catch (ex: Exception) {
                        println("A exception occurred during invoking ${item.first::class.qualifiedName}::${item.second.name}")
                        ex.cause?.printStackTrace()
                    }
                }
            }
        }
    }
}