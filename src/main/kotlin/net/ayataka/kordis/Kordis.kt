package net.ayataka.kordis

import org.apache.logging.log4j.LogManager

object Kordis {
    const val NAME = "Kordis"
    const val VERSION = "0.0.1-SNAPSHOT"
    const val URL = "https://github.com/Tea-Ayataka/Kordis"
    const val API_VERSION = 6

    val LOGGER = LogManager.getLogger()!!

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