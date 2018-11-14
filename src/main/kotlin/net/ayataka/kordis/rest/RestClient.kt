package net.ayataka.kordis.rest

import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
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

class RestClient(private val discordClient: DiscordClientImpl) {
    private val rateLimiter = InternalRateLimiter()

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
                        return@apply
                    }

                    if (endPoint.method == HttpMethod.GET) {
                        url(endPoint.url + "?" + data.entries.joinToString("&") { "${it.key}=${it.value}" })
                        return@apply
                    }

                    method(endPoint.method.name, RequestBody.create(JSON_TYPE, data.toString()))
                }.build()

                val response = HTTP_CLIENT.newCall(request).executeAsync()

                val json = if (response.headers()["Content-Type"] == "application/json") {
                    response.body()?.let { JsonTreeParser(it.string()).readFully() }
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

                    val delay = json.jsonObject["retry_after"].long

                    if (json.jsonObject["global"].boolean) {
                        rateLimiter.setGlobalRateLimitEnds(delay)
                    } else {
                        rateLimiter.setRateLimitEnds(endPoint, System.currentTimeMillis() + delay)

                        if (endPoint.endpoint != Endpoint.CREATE_GUILD_EMOJI
                                && endPoint.endpoint != Endpoint.MODIFY_GUILD_EMOJI
                                && endPoint.endpoint != Endpoint.DELETE_GUILD_EMOJI
                        ) {
                            LOGGER.warn("HIT ACTUAL RATE LIMIT! MAKE SURE YOUR COMPUTER'S CLOCK IS CORRECT! ($delay ms)")
                        }
                    }

                    delay(delay)
                    return@repeat
                }

                if (response.code() in 500..599) {
                    throw Exception("Discord API returned internal server error (code: ${response.code()})") // Retry
                }

                if (response.code() == 404) {
                    throw NotFoundException()
                }

                if (response.code() !in 200..299) {
                    throw DiscordException("Discord API returned status code ${response.code()} with body ${json?.toString()}", response.code())
                }

                if (response.headers()["X-RateLimit-Limit"] != null && response.headers()["X-RateLimit-Reset"] != null) {
                    val rateLimit = response.headers()["X-RateLimit-Limit"]!!.toInt()
                    val rateLimitEnds = response.headers()["X-RateLimit-Reset"]!!.toLong() * 1000
                    rateLimiter.setRateLimit(endPoint, rateLimit)
                    rateLimiter.setRateLimitEnds(endPoint, rateLimitEnds)
                    LOGGER.debug("RateLimit: $rateLimit, Remaining: ${response.headers()["X-RateLimit-Remaining"]}, Ends: $rateLimitEnds")
                }

                return json ?: JsonObject(emptyMap())
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