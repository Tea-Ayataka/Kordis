package net.ayataka.kordis.rest

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.Kordis.LOGGER
import java.util.concurrent.ConcurrentHashMap

class InternalRateLimiter {
    @Volatile
    private var globalRateLimitEnds: Long = 0L

    private val mutex = Mutex()
    private val mutexes = ConcurrentHashMap<Int, Mutex>()
    private val rateLimits = ConcurrentHashMap<Int, Int>()
    private val rateLimitEnds = ConcurrentHashMap<Int, Long>()
    private val rateLimitRemaining = ConcurrentHashMap<Int, Int>()

    internal suspend fun getMutex(endPoint: Endpoint) = mutex.withLock { mutexes.getOrPut(endPoint.majorHash()) { Mutex() } }

    suspend fun setGlobalRateLimitEnds(delay: Long) = mutex.withLock {
        globalRateLimitEnds = System.currentTimeMillis() + delay
        LOGGER.info("Hit global rate limit ($delay ms)")
    }

    suspend fun setRateLimitEnds(endPoint: Endpoint, time: Long) = mutex.withLock {
        rateLimitEnds[endPoint.majorHash()] = Math.max(rateLimitEnds[endPoint.majorHash()]
                ?: 0, time)
    }

    suspend fun setRateLimit(endPoint: Endpoint, value: Int) = mutex.withLock {
        // First time
        if (rateLimits[endPoint.majorHash()] == null) {
            rateLimitRemaining[endPoint.majorHash()] = value - 1
        }

        rateLimits[endPoint.majorHash()] = value
    }

    suspend fun incrementRateLimitRemaining(endPoint: Endpoint) = mutex.withLock {
        rateLimitRemaining[endPoint.majorHash()] = (rateLimitRemaining[endPoint.majorHash()] ?: return) + 1
    }

    suspend fun setRateLimitRemaining(endPoint: Endpoint, value: Int) = mutex.withLock {
        rateLimitRemaining[endPoint.majorHash()] = value
    }

    suspend fun wait(endPoint: Endpoint) {
        // Wait for global rate limit
        mutex.withLock {
            if (globalRateLimitEnds > System.currentTimeMillis()) {
                delay(globalRateLimitEnds - System.currentTimeMillis())
            }
        }

        // Wait for per route rate limit
        rateLimitRemaining[endPoint.majorHash()]?.let {
            // If the remaining count is 0
            if (it <= 0) {
                // Wait until it's reset
                while (Math.max(globalRateLimitEnds, rateLimitEnds[endPoint.majorHash()]!!) > System.currentTimeMillis()) {
                    val delay = Math.max(globalRateLimitEnds, rateLimitEnds[endPoint.majorHash()]!!) - System.currentTimeMillis()
                    LOGGER.debug("Hit a rate limit internally. Wait ${delay}ms")
                    delay(delay)
                }

                // Reset
                rateLimitRemaining[endPoint.majorHash()] = rateLimits[endPoint.majorHash()]!!
            }
        }
    }
}