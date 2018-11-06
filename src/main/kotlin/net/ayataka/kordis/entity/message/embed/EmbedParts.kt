package net.ayataka.kordis.entity.message.embed

data class Field(val name: String?, val value: String?, val inline: Boolean)
data class Footer(val text: String?, val iconUrl: String?)
data class Author(val name: String?, val url: String?, val iconUrl: String?)