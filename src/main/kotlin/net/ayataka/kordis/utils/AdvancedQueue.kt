package net.ayataka.kordis.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class AdvancedQueue<T>(val amount: Int, val action: suspend (List<T>) -> Unit) {
    private val queue = LinkedList<T>()
    private var job = Job()
    private val locker = Mutex()

    @Volatile
    private var isCompleted = true

    fun offer(vararg values: T) = GlobalScope.start {
        locker.withLock {
            queue.addAll(values)

            if (isCompleted) {
                isCompleted = false
                job = launch {
                    while (true) {
                        val taken = locker.withLock {
                            val taken = queue.take(amount)
                            queue.removeAll(taken)
                            taken
                        }

                        if (taken.isNotEmpty()) {
                            try {
                                action(taken)
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }

                        locker.withLock {
                            if (queue.isEmpty()) {
                                isCompleted = true
                                return@launch
                            }
                        }
                    }
                }
            }
        }
    }

    fun clear() = runBlocking {
        locker.withLock {
            queue.clear()
        }
    }
}