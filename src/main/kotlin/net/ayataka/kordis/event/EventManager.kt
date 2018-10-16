package net.ayataka.kordis.event

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class EventManager {
    private val dispatcher = CoroutineScope(Dispatchers.Default + CoroutineName("Event Dispatcher"))
    private val handlers = hashMapOf<KClass<out Event>, MutableList<Triple<Any, KFunction<*>, EventPriority>>>()

    private fun getAnnotatedFunctions(instance: Any): List<KFunction<*>> {
        return instance::class.functions // Get functions
                .filter { it.annotations.any { it is EventListener } }  // Annotation check
                .filter { it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>).isSubclassOf(Event::class) } // Parameter check
    }

    fun register(handler: Any) {
        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>
            val annotation = it.annotations.find { it is EventListener } as EventListener

            if (handlers[param] == null) {
                handlers[param] = mutableListOf()
            }

            handlers[param]!!.add(Triple(handler, it, annotation.priority))
        }
    }

    fun unregister(handler: Any) {
        getAnnotatedFunctions(handler).forEach {
            @Suppress("UNCHECKED_CAST")
            val param: KClass<out Event> = it.parameters[1].type.classifier as KClass<out Event>

            handlers[param]?.let {
                it.removeIf { it.first == handler }
            }
        }
    }

    fun fire(event: Any) {
        if (event !is Event) {
            return
        }

        handlers[event::class]?.let {
            for (item in it.sortedBy { it.third.order }) {
                dispatcher.launch {
                    try {
                        item.second.call(item.first, event)
                    } catch (ex: Exception) {
                        println("A exception occurred during invoking ${item.first::class.qualifiedName}::${item.second.name}")
                        ex.cause?.printStackTrace()
                    }
                }
            }
        }
    }
}