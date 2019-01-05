package net.ayataka.kordis

import org.slf4j.LoggerFactory
import java.net.http.HttpClient

object Kordis {
    const val NAME = "Kordis"
    const val VERSION = "0.1.3"
    const val URL = "https://github.com/Tea-Ayataka/Kordis"
    const val API_VERSION = 6

    val HTTP_CLIENT = HttpClient.newHttpClient()!!
    val LOGGER = LoggerFactory.getLogger(NAME)!!

    init {
        LOGGER.info("$NAME v$VERSION ($URL)")
    }

    suspend fun create(block: DiscordClientBuilder.() -> Unit): DiscordClient {
        DiscordClientBuilder().apply(block).run {
            if (token.isEmpty()) {
                throw IllegalArgumentException("Bot token must be specified")
            }

            val client = DiscordClientImpl(token, shard, maxShard)
            listeners.forEach { client.addListener(it) }
            handlers.forEach { client.addListener(it) }
            client.connect()
            return client
        }
    }
}