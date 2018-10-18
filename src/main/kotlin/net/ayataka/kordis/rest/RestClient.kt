package net.ayataka.kordis.rest

import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.LOGGER

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
                LOGGER.info("Request: ${endPoint.url}, method: ${endPoint.method.value}, data: $data")
                val call = httpClient.call {
                    method = endPoint.method
                    url(endPoint.url)
                    if (data != null) {
                        body = data
                    }
                }

                val json = if (call.response.contentType() == ContentType.Application.Json) {
                    JsonParser().parse(call.response.readText(Charsets.UTF_8)).asJsonObject
                } else {
                    null
                }

                if (call.response.status == HttpStatusCode.TooManyRequests) {
                    if (json != null) {
                        val retryDelay = json["retry_after"].asLong
                        val isGlobal = json["global"].asBoolean

                        if (isGlobal) {
                            globalRateLimitEnds = System.currentTimeMillis() + retryDelay
                            LOGGER.info("Hit global rate limit ($retryDelay ms)")
                        } else {
                            LOGGER.info("Hit rate limit ($retryDelay ms)")
                        }

                        delay(retryDelay)
                    } else {
                        throw RateLimitedException()
                    }
                }

                if (call.response.status != HttpStatusCode.OK) {
                    throw DiscordException("Discord API returned an error with status code ${call.response.status.value} and body ${json?.toString()}")
                }

                if (json == null) {
                    throw DiscordException("Discord API returned an invalid result: ${call.response.receive<String>()}")
                }

                LOGGER.info("Response: $json")
                return json
            } catch (ex: DiscordException) {
                throw ex
            } catch (ex: RateLimitedException) {
                throw ex
            } catch (ex: Exception) {
                ex.printStackTrace()
                LOGGER.warn("An unknown exception thrown! retrying in 3 sec")
                delay(3000)
            }
        }

        throw RateLimitedException()
    }
}