package net.ayataka.kordis.utils

class RateLimiter(private val period: Long, private val rate: Int) {
    private val times = mutableListOf<Long>()

    fun isLimited(): Boolean {
        synchronized(times) {
            val currentTime = System.currentTimeMillis()
            times.removeIf { it < currentTime - period }
            return times.size >= rate
        }
    }

    fun increment() {
        synchronized(times) {
            times.add(System.currentTimeMillis())
        }
    }
}