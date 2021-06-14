package net.ayataka.kordis.event

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.event.events.message.MessageEvent
import net.ayataka.kordis.event.events.server.ServerEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class EventManager {
    private val dispatcher = CoroutineScope(Dispatchers.Default + CoroutineName("Event Dispatcher"))
    private val channel = Channel<Event>(Channel.UNLIMITED)
    private val handlerMap = ConcurrentHashMap<KClass<out Event>, ConcurrentHashMap<Long, CopyOnWriteArrayList<Pair<Any?, Any>>>>()

    init {
        @Suppress("EXPERIMENTAL_API_USAGE")
        GlobalScope.launch(newSingleThreadContext("Event Dispatcher")) {
            for (event in channel) {
                val handlers = mutableListOf<Pair<Any?, Any>>()

                handlerMap[event::class]?.get(-1)?.let { handlers.addAll(it) }

                when (event) {
                    is ServerEvent -> handlerMap[event::class]?.get(event.server.id)?.let { handlers.addAll(it) }
                    is MessageEvent -> event.server?.let { handlerMap[event::class]?.get(it.id)?.let { handlers.addAll(it) } }
                }

                handlers.forEach {
                    dispatcher.launch {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            (it.second as? Listener<Event>)?.action?.invoke(event)
                                    ?: (it.second as? KFunction<*>)?.callSuspend(it.first, event)
                        } catch (ex: Exception) {
                            LOGGER.error("An exception occurred during invoking ${it.second}", ex.cause ?: ex)
                        }
                    }
                }
            }

        }
    }

    private fun getAnnotatedFunctions(instance: Any): List<KFunction<*>> {
        return instance::class.functions // Get functions
                .filter { it.annotations.any { it is EventHandler } }  // Annotation check
                .filter { it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>).isSubclassOf(Event::class) } // Parameter check
    }

    fun register(handler: Any, serverId: Long?) {
        if (handler is Listener<*>) {
            handlerMap.getOrPut(handler.event) { ConcurrentHashMap() }.getOrPut(serverId ?: -1) { CopyOnWriteArrayList() }.add(Pair(null, handler))
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>
            handlerMap.getOrPut(param) { ConcurrentHashMap() }.getOrPut(serverId ?: -1) { CopyOnWriteArrayList() }.add(Pair(handler, it))
        }
    }

    fun unregister(handler: Any, serverId: Long?) {
        if (handler is Listener<*>) {
            handlerMap[handler.event]?.let {
                it[serverId ?: -1]?.removeIf { it.second == handler }
            }
            return
        }

        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>

            handlerMap[param]?.let {
                it[serverId ?: -1]?.removeIf { it.first == handler }
            }
        }
    }

    /**
     * Dispatch an event.
     */
    fun fire(event: Event) {
        channel.offer(event)
    }
}