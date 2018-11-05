package net.ayataka.kordis

object Kordis {
    fun create(block: DiscordClientBuilder.() -> Unit): DiscordClient {
        val builder = DiscordClientBuilder()
        block(builder)

        if (builder.token == null) {
            throw IllegalArgumentException("Bot token must be specified")
        }

        return DiscordClientImpl(builder.token!!, builder.shards, builder.maxShards)
    }
}
