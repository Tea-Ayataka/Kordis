package net.ayataka.kordis.rest

class RateLimitedException : Exception()
class DiscordException(override val message: String?) : Exception()