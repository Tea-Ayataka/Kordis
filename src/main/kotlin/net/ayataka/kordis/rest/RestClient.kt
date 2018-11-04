package net.ayataka.kordis.rest

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTreeParser
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.LOGGER
import java.util.concurrent.TimeoutException

class RestClient(private val discordClient: DiscordClient) {
    private val rateLimiter = InternalRateLimiter()
    private val httpClient = HttpClient {
        defaultRequest {
            header(HttpHeaders.Authorization, "Bot ${discordClient.token}")
            header(HttpHeaders.UserAgent, "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")
        }
    }

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonObject {
        repeat(rateLimitRetries) {
            rateLimiter.wait(endPoint)

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.value}, data: $data, retry: $it / $rateLimitRetries : ${System.currentTimeMillis() / 1000}")
                val call = httpClient.call {
                    method = endPoint.method
                    url(endPoint.url)
                    if (data != null) {
                        body = data
                    }
                }

                // Receive the body
                val json = if (call.response.contentType() == ContentType.Application.Json) {
                    JsonTreeParser(call.response.readText(Charsets.UTF_8)).readFully().jsonObject
                } else {
                    null
                }

                if (call.response.status != HttpStatusCode.OK) {
                    rateLimiter.incrementRateLimitRemaining(endPoint)
                }

                // Handle rate limits
                if (call.response.status.value == HttpStatusCode.TooManyRequests.value) {
                    if (json == null) {
                        // When get rate limited without body
                        throw RateLimitedException()
                    }

                    val delay = json["retry_after"].long

                    if (json["global"].boolean) {
                        rateLimiter.setGlobalRateLimitEnds(delay)
                    } else {
                        rateLimiter.setRateLimitEnds(endPoint, System.currentTimeMillis() + delay)
                        LOGGER.warn("HIT ACTUAL RATE LIMIT! MAKE SURE YOUR COMPUTER'S CLOCK IS CORRECT! ($delay ms)")
                    }

                    delay(delay)
                    return@repeat
                }

                if (call.response.status.value == HttpStatusCode.GatewayTimeout.value) {
                    throw TimeoutException() // Retry
                }

                if (call.response.status != HttpStatusCode.OK) {
                    throw DiscordException("Discord API returned status code ${call.response.status.value} (${call.response.status.description}) with body ${json?.toString()}")
                }

                if (call.response.headers["X-RateLimit-Limit"] != null && call.response.headers["X-RateLimit-Reset"] != null) {
                    val rateLimit = call.response.headers["X-RateLimit-Limit"]!!.toInt()
                    val rateLimitEnds = call.response.headers["X-RateLimit-Reset"]!!.toLong() * 1000
                    rateLimiter.setRateLimit(endPoint, rateLimit)
                    rateLimiter.setRateLimitEnds(endPoint, rateLimitEnds)
                    LOGGER.trace("RateLimit: $rateLimit, Remaining: ${call.response.headers["X-RateLimit-Remaining"]}, Ends: $rateLimitEnds")
                }

                if (json == null) {
                    throw DiscordException("Discord API returned an invalid result: ${call.response.receive<String>()}")
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