package net.ayataka.kordis.rest

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.withLock
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.Kordis.HTTP_CLIENT
import net.ayataka.kordis.Kordis.LOGGER
import net.ayataka.kordis.exception.DiscordException
import net.ayataka.kordis.exception.MissingPermissionsException
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.exception.RateLimitedException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RestClient(private val discordClient: DiscordClientImpl) {
    private val gson = Gson()
    private val rateLimiter = InternalRateLimiter()

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonElement = rateLimiter.getMutex(endPoint).withLock {
        repeat(rateLimitRetries) {
            rateLimiter.wait(endPoint)

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.name}, data: $data, retry: $it / $rateLimitRetries")

                val request = HttpRequest.newBuilder().apply {
                    uri(URI(endPoint.url))
                    header("Accept", "application/json")
                    header("Authorization", "Bot ${discordClient.token}")
                    header("User-Agent", "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")

                    if (data == null) {
                        if (endPoint.method == HttpMethod.GET) {
                            return@apply
                        }

                        header("Content-Type", "application/json")
                        method(endPoint.method.name, HttpRequest.BodyPublishers.ofString("{}"))
                        return@apply
                    }

                    if (endPoint.method == HttpMethod.GET) {
                        uri(URI.create(endPoint.url + "?" + data.entrySet().joinToString("&") { "${it.key}=${it.value}" }))
                        return@apply
                    }

                    header("Content-Type", "application/json")
                    method(endPoint.method.name, HttpRequest.BodyPublishers.ofString(data.toString()))
                }.build()

                val response = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                val contentType = response.headers().firstValue("Content-Type").orElse(null)
                val body = response.body()

                val json = if (contentType?.equals("application/json", true) == true) {
                    body?.let { gson.fromJson(body, JsonElement::class.java) }
                } else {
                    null
                }

                LOGGER.debug("Response: ${response.statusCode()}, body: $json")

                // Update rate limits
                val rateLimit = response.headers().firstValue("X-RateLimit-Limit").orElse(null)?.toInt()
                val rateLimitRemaining = response.headers().firstValue("X-RateLimit-Remaining").orElse(null)?.toInt()
                val rateLimitEnds = response.headers().firstValue("X-RateLimit-Reset").orElse(null)?.toLong()

                if (response.statusCode() !in 200..299) {
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
                if (response.statusCode() == 429) {
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

                if (response.statusCode() in 500..599) {
                    throw Exception("Discord API returned internal server error (code: ${response.statusCode()})") // Retry
                }

                if (response.statusCode() == 403) {
                    throw MissingPermissionsException("Request: ${endPoint.url}, Response: $response")
                }

                if (response.statusCode() == 404) {
                    throw NotFoundException(true)
                }

                if (response.statusCode() !in 200..299) {
                    throw DiscordException("Discord API returned status code ${response.statusCode()} with body ${json?.toString()}", response.statusCode())
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