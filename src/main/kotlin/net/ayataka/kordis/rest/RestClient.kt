package net.ayataka.kordis.rest

import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.LOGGER
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.RateLimitedException
import net.ayataka.kordis.utils.executeAsync
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

private val JSON_TYPE = MediaType.parse("application/json; charset=utf-8")!!

class RestClient(private val discordClient: DiscordClientImpl) {
    private val rateLimiter = InternalRateLimiter()
    private val httpClient = OkHttpClient()

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonElement {
        repeat(rateLimitRetries) {
            rateLimiter.wait(endPoint)

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.name}, data: $data, retry: $it / $rateLimitRetries")

                val request = Request.Builder().apply {
                    url(endPoint.url)
                    header("Authorization", "Bot ${discordClient.token}")
                    header("User-Agent", "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")

                    if (data == null) {
                        method(endPoint.method.name, null)
                    } else {
                        method(endPoint.method.name, RequestBody.create(JSON_TYPE, data.toString()))
                    }
                }.build()

                val response = httpClient.newCall(request).executeAsync()

                val json = if (response.headers()["Content-Type"] == "application/json") {
                    response.body()?.let { JsonTreeParser(it.string()).readFully() }
                } else {
                    null
                }

                if (response.code() !in 200..299) {
                    rateLimiter.incrementRateLimitRemaining(endPoint)
                }

                // Handle rate limits (429 Too Many Requests)
                if (response.code() == 429) {
                    if (json == null) {
                        // When get rate limited without body
                        throw RateLimitedException()
                    }

                    val delay = json.jsonObject["retry_after"].long

                    if (json.jsonObject["global"].boolean) {
                        rateLimiter.setGlobalRateLimitEnds(delay)
                    } else {
                        rateLimiter.setRateLimitEnds(endPoint, System.currentTimeMillis() + delay)
                        LOGGER.warn("HIT ACTUAL RATE LIMIT! MAKE SURE YOUR COMPUTER'S CLOCK IS CORRECT! ($delay ms)")
                    }

                    delay(delay)
                    return@repeat
                }

                if (response.code() in 500..599) {
                    throw Exception("Discord API returned internal server error (code: ${response.code()})") // Retry
                }

                if (response.code() !in 200..299) {
                    throw DiscordException("Discord API returned status code ${response.code()} with body ${json?.toString()}")
                }

                if (response.headers()["X-RateLimit-Limit"] != null && response.headers()["X-RateLimit-Reset"] != null) {
                    val rateLimit = response.headers()["X-RateLimit-Limit"]!!.toInt()
                    val rateLimitEnds = response.headers()["X-RateLimit-Reset"]!!.toLong() * 1000
                    rateLimiter.setRateLimit(endPoint, rateLimit)
                    rateLimiter.setRateLimitEnds(endPoint, rateLimitEnds)
                    LOGGER.debug("RateLimit: $rateLimit, Remaining: ${response.headers()["X-RateLimit-Remaining"]}, Ends: $rateLimitEnds")
                }

                if (json == null) {
                    throw DiscordException("Discord API returned an invalid result: ${response.body()}")
                }

                LOGGER.debug("Response: $json")
                return json
            } catch (ex: DiscordException) {
                throw ex
            } catch (ex: RateLimitedException) {
                throw ex
            } catch (ex: Exception) {
                LOGGER.warn("An unexpected exception thrown! retrying in 3 sec", ex)
                delay(3000)
            }
        }

        throw RateLimitedException()
    }
}