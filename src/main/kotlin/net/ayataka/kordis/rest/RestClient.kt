package net.ayataka.kordis.rest

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.HTTP_CLIENT
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.MissingPermissionsException
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.exception.RateLimitedException

class RestClient(private val discordClient: DiscordClientImpl) {
    private val gson = Gson()
    private val rateLimiter = InternalRateLimiter()

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonElement = rateLimiter.getMutex(endPoint).withLock {
        repeat(rateLimitRetries) {
            rateLimiter.wait(endPoint)

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.value}, data: $data, retry: $it / $rateLimitRetries")

                val response = HTTP_CLIENT.call {
                    method = endPoint.method
                    url(endPoint.url)
                    header(HttpHeaders.Accept, "application/json")
                    header(HttpHeaders.Authorization, "Bot ${discordClient.token}")
                    header(HttpHeaders.UserAgent, "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")

                    if (endPoint.method == HttpMethod.Get) {
                        if (data != null) {
                            url(endPoint.url + "?" + data.entrySet().joinToString("&") { "${it.key}=${it.value}" })
                        }
                        return@call
                    }

                    body = TextContent(data?.toString() ?: "{}", ContentType.Application.Json)
                }.response

                val contentType = response.headers["Content-Type"]
                val body = response.readText()

                val json = if (contentType?.equals("application/json", true) == true) {
                    gson.fromJson(body, JsonElement::class.java)
                } else {
                    null
                }

                LOGGER.debug("Response: ${response.status.value}, body: $json")

                // Update rate limits
                val rateLimit = response.headers["X-RateLimit-Limit"]?.toInt()
                val rateLimitRemaining = response.headers["X-RateLimit-Remaining"]?.toInt()
                val rateLimitEnds = response.headers["X-RateLimit-Reset"]?.toLong()

                if (response.status.value !in 200..299) {
                    // When failed to request
                    rateLimiter.incrementRateLimitRemaining(endPoint)
                }

                if (rateLimit != null && rateLimitRemaining != null && rateLimitEnds != null) {
                    rateLimiter.setRateLimit(endPoint, rateLimit)
                    rateLimiter.setRateLimitRemaining(endPoint, rateLimitRemaining)
                    rateLimiter.setRateLimitEnds(endPoint, rateLimitEnds * 1000)
                    LOGGER.debug("RateLimit: $rateLimit, Remaining: $rateLimitRemaining, Ends: $rateLimitEnds")
                }

                // Handle rate limits (429 Too Many Requests)
                if (response.status.value == 429) {
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

                if (response.status.value in 500..599) {
                    throw Exception("Discord API returned internal server error (code: ${response.status.value})") // Retry
                }

                if (response.status.value == 403) {
                    throw MissingPermissionsException("Request: ${endPoint.url}, Response: $response")
                }

                if (response.status.value == 404) {
                    throw NotFoundException(true)
                }

                if (response.status.value !in 200..299) {
                    throw DiscordException("Discord API returned status code ${response.status.value} with body ${json?.toString()}", response.status.value)
                }

                return json ?: JsonObject()
            } catch (ex: DiscordException) {
                throw ex
            } catch (ex: RateLimitedException) {
                throw ex
            } catch (ex: NotFoundException) {
                throw ex
            } catch (ex: MissingPermissionsException) {
                throw ex
            } catch (ex: Exception) {
                LOGGER.warn("An unexpected error has occurred! we will retry in a second", ex)
                delay(1000)
            }
        }

        throw RateLimitedException()
    }
}