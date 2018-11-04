package net.ayataka.kordis.rest

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.LOGGER
import java.util.concurrent.ConcurrentHashMap

class InternalRateLimiter {
    @Volatile
    private var globalRateLimitEnds: Long = 0L

    private val mutex = Mutex()
    private val mutexes = ConcurrentHashMap<Int, Mutex>()
    private val rateLimits = ConcurrentHashMap<Int, Int>()
    private val rateLimitEnds = ConcurrentHashMap<Int, Long>()
    private val rateLimitRemaining = ConcurrentHashMap<Int, Int>()

    fun setGlobalRateLimitEnds(delay: Long) {
        globalRateLimitEnds = System.currentTimeMillis() + delay
        LOGGER.info("Hit global rate limit ($delay ms)")
    }

    fun setRateLimitEnds(endPoint: FormattedEndPoint, time: Long) {
        rateLimitEnds[endPoint.majorHash()] = Math.max(rateLimitEnds[endPoint.majorHash()]
                ?: 0, time)
    }

    fun setRateLimit(endPoint: FormattedEndPoint, value: Int) {
        // First time
        if (rateLimits[endPoint.majorHash()] == null) {
            rateLimitRemaining[endPoint.majorHash()] = value - 1
        }

        rateLimits[endPoint.majorHash()] = value
    }

    fun incrementRateLimitRemaining(endPoint: FormattedEndPoint) {
        rateLimitRemaining[endPoint.majorHash()] = (rateLimitRemaining[endPoint.majorHash()] ?: return) + 1
    }

    suspend fun wait(endPoint: FormattedEndPoint) {
        // Wait for global rate limit
        mutex.withLock {
            if (globalRateLimitEnds > System.currentTimeMillis()) {
                delay(globalRateLimitEnds - System.currentTimeMillis())
            }
        }

        // Wait for per route rate limit
        mutexes.getOrPut(endPoint.majorHash()) { Mutex() }.withLock {
            rateLimitRemaining[endPoint.majorHash()]?.let {
                // If the remaining count is 0
                if (it <= 0) {
                    val rateLimitEnds = rateLimitEnds[endPoint.majorHash()]!!

                    // Wait until it's reset
                    println("Wait: ${(rateLimitEnds - System.currentTimeMillis())}")
                    delay((rateLimitEnds - System.currentTimeMillis()))

                    // Reset
                    rateLimitRemaining[endPoint.majorHash()] = rateLimits[endPoint.majorHash()]!!
                }

                // Decrement
                rateLimitRemaining[endPoint.majorHash()] = (rateLimitRemaining[endPoint.majorHash()] ?: return) - 1
            }
        }
    }
}