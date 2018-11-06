package net.ayataka.kordis.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

private val mutexes = ConcurrentHashMap<KProperty<*>, Mutex>()

suspend fun <T> KProperty<*>.withLock(action: suspend () -> T): T {
    return mutexes.getOrPut(this) { Mutex() }.withLock { action() }
}