package net.ayataka.kordis.rest

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.HTTP_CLIENT
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.exception.RateLimitedException
import net.ayataka.kordis.utils.executeAsync
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

private val JSON_TYPE = MediaType.parse("application/json; charset=utf-8")!!
private val EMPTY_BODY = RequestBody.create(JSON_TYPE, "")

class RestClient(private val discordClient: DiscordClientImpl) {
    private val gson = Gson()
    private val rateLimiter = InternalRateLimiter()

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonElement = rateLimiter.getMutex(endPoint).withLock {
        repeat(rateLimitRetries) {
            rateLimiter.wait(endPoint)

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.name}, data: $data, retry: $it / $rateLimitRetries")

                val request = Request.Builder().apply {
                    url(endPoint.url)
                    header("Authorization", "Bot ${discordClient.token}")
                    header("User-Agent", "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")

                    if (data == null) {
                        method(endPoint.method.name, if (endPoint.method != HttpMethod.GET) EMPTY_BODY else null)
                        return@apply
                    }

                    if (endPoint.method == HttpMethod.GET) {
                        url(endPoint.url + "?" + data.entrySet().joinToString("&") { "${it.key}=${it.value}" })
                        return@apply
                    }

                    method(endPoint.method.name, RequestBody.create(JSON_TYPE, data.toString()))
                }.build()

                val response = HTTP_CLIENT.newCall(request).executeAsync()
                val body = response.body()?.string()
                response.body()?.close()

                val json = if (response.headers()["Content-Type"] == "application/json") {
                    body?.let { gson.fromJson(body, JsonElement::class.java) }
                } else {
                    null
                }

                LOGGER.debug("Response: ${response.code()} ${response.message()}, body: $json")

                if (response.code() !in 200..299) {
                    rateLimiter.incrementRateLimitRemaining(endPoint)
                }

                // Handle rate limits (429 Too Many Requests)
                if (response.code() == 429) {
                    if (json == null) {
                        // When get rate limited without body
                        throw RateLimitedException()
                    }

                    val delay = json.asJsonObject["retry_after"].asLong

                    if (json.asJsonObject["global"].asBoolean) {
                        rateLimiter.setGlobalRateLimitEnds(delay)
                    } else {
                        rateLimiter.setRateLimitEnds(endPoint, System.currentTimeMillis() + delay)
                        rateLimiter.setRateLimitRemaining(endPoint, 0)
                    }

                    return@repeat
                }

                if (response.code() in 500..599) {
                    throw Exception("Discord API returned internal server error (code: ${response.code()})") // Retry
                }

                if (response.code() == 404) {
                    throw NotFoundException(true)
                }

                if (response.code() !in 200..299) {
                    throw DiscordException("Discord API returned status code ${response.code()} with body ${json?.toString()}", response.code())
                }

                if (response.headers()["X-RateLimit-Limit"] != null && response.headers()["X-RateLimit-Reset"] != null) {
                    val rateLimit = response.headers()["X-RateLimit-Limit"]!!.toInt()
                    val rateLimitRemaining = response.headers()["X-RateLimit-Remaining"]!!.toInt()
                    val rateLimitEnds = response.headers()["X-RateLimit-Reset"]!!.toLong() * 1000
                    rateLimiter.setRateLimit(endPoint, rateLimit)
                    rateLimiter.setRateLimitRemaining(endPoint, rateLimitRemaining)
                    rateLimiter.setRateLimitEnds(endPoint, rateLimitEnds)
                    LOGGER.debug("RateLimit: $rateLimit, Remaining: ${response.headers()["X-RateLimit-Remaining"]}, Ends: $rateLimitEnds")
                }

                return json ?: JsonObject()
            } catch (ex: DiscordException) {
                throw ex
            } catch (ex: RateLimitedException) {
                throw ex
            } catch (ex: NotFoundException) {
                throw ex
            } catch (ex: Exception) {
                LOGGER.warn("An unexpected error has occurred! we will retry in a second", ex)
                delay(1000)
            }
        }

        throw RateLimitedException()
    }
}