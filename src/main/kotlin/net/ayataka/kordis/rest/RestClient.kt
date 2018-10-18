package net.ayataka.kordis.rest

import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import net.ayataka.kordis.DiscordClient

class RestClient(private val discordClient: DiscordClient) {
    private val httpClient = HttpClient(CIO) {
        install(JsonFeature)
        defaultRequest {
            header(HttpHeaders.Authorization, "Bot ${discordClient.token}")
        }
    }

    suspend fun get(endPoint: EndPoint, vararg args: String): JsonObject {
        return httpClient.get(String.format(endPoint.link, args))
    }

    suspend fun post(endPoint: EndPoint, json: JsonObject? = null, vararg args: String): JsonObject {
        return httpClient.post(String.format(endPoint.link, args)) { json?.let { body = it } }
    }
}