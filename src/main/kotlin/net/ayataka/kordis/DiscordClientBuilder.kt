package net.ayataka.kordis

class DiscordClientBuilder {
    val listeners = mutableListOf<Any>()
    var token: String? = null
    var shard: Int = 0
    var maxShards: Int = 0
}

fun DiscordClientBuilder.addListener(listener: Any) = listeners.add(listener)