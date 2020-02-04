package net.ayataka.kordis

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Protocol
import org.slf4j.LoggerFactory

object Kordis {
    const val NAME = "Kordis"
    const val VERSION = "0.2.2"
    const val URL = "https://github.com/Tea-Ayataka/Kordis"
    const val API_VERSION = 6

    val LOGGER = LoggerFactory.getLogger(NAME)!!
    val HTTP_CLIENT = HttpClient(OkHttp) {
        engine {
            config {
                protocols(listOf(Protocol.HTTP_1_1))
            }
        }

        expectSuccess = false
    }

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