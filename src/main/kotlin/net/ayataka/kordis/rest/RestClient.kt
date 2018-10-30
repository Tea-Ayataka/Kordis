package net.ayataka.kordis.rest

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.LOGGER
import java.util.concurrent.TimeoutException

class RestClient(private val discordClient: DiscordClient) {
    private val httpClient = HttpClient {
        install(JsonFeature)
        defaultRequest {
            header(HttpHeaders.Authorization, "Bot ${discordClient.token}")
            header(HttpHeaders.UserAgent, "DiscordBot (https://github.com/Tea-Ayataka/Kordis, development)")
        }
    }

    @Volatile
    private var globalRateLimitEnds: Long = 0L

    suspend fun request(endPoint: FormattedEndPoint, data: JsonObject? = null, rateLimitRetries: Int = 50): JsonObject {
        repeat(rateLimitRetries) {
            if (globalRateLimitEnds > System.currentTimeMillis()) {
                delay(globalRateLimitEnds - System.currentTimeMillis())
            }

            try {
                LOGGER.debug("Request: ${endPoint.url}, method: ${endPoint.method.value}, data: $data, retry: $it / $rateLimitRetries")
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

                // Handle rate limits
                if (call.response.status == HttpStatusCode.TooManyRequests) {
                    if (json == null) {
                        // When get rate limited without body
                        throw RateLimitedException()
                    }

                    val delay = json["retry_after"].long

                    if (json["global"].boolean) {
                        globalRateLimitEnds = System.currentTimeMillis() + delay
                        LOGGER.info("Hit global rate limit ($delay ms)")
                    } else {
                        LOGGER.info("Hit rate limit ($delay ms)")
                    }

                    delay(delay)
                }

                if (call.response.status == HttpStatusCode.GatewayTimeout) {
                    throw TimeoutException() // Retry
                }

                if (call.response.status != HttpStatusCode.OK) {
                    throw DiscordException("Discord API returned status code ${call.response.status.value} (${call.response.status.description}) with body ${json?.toString()}")
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