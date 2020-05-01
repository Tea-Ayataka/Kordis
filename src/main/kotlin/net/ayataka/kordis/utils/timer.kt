package net.ayataka.kordis.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.ayataka.kordis.Kordis.LOGGER
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis

/**
 * Creates a timer that executes the specified [action] periodically.
 */
fun CoroutineScope.timer(
        interval: Long,
        fixedRate: Boolean = true,
        context: CoroutineContext = EmptyCoroutineContext,
        action: suspend TimerScope.() -> Unit
) = launch(context) {
    val scope = TimerScope()

    while (isActive) {
        val time = measureTimeMillis {
            try {
                action(scope)
            } catch (ex: Exception) {
                LOGGER.error("Coroutine Timer", ex)
            }
        }

        if (scope.isCanceled) {
            break
        }

        if (fixedRate) {
            delay(Math.max(0, interval - time))
        } else {
            delay(interval)
        }
    }
}


class TimerScope {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }
}