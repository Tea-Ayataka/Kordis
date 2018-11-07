package net.ayataka.kordis

import org.apache.logging.log4j.LogManager

const val NAME = "Kordis"
const val VERSION = "0.0.1-SNAPSHOT"
const val URL = "https://github.com/Tea-Ayataka/Kordis"
val LOGGER = LogManager.getLogger()!!

object Kordis {
    init {
        LOGGER.debug("$NAME v$VERSION ($URL)")
    }

    suspend fun create(block: DiscordClientBuilder.() -> Unit): DiscordClient {
        val builder = DiscordClientBuilder()
        block(builder)

        if (builder.token == null) {
            throw IllegalArgumentException("Bot token must be specified")
        }

        val client = DiscordClientImpl(builder.token!!, builder.shard, builder.maxShards, builder.listeners)
        client.connect()
        return client
    }
}
