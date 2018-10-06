package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient

class UserImpl(override val client: DiscordClient, json: JsonObject) : User {
    private var _id: Long? = null
    private var _name: String? = null
    private var _discriminator: Int? = null

    override val id get() = _id!!
    override val name get() = _name!!
    override val discriminator get() = _discriminator!!

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        _id = json["id"].primitive.long
        _name = "test"
        _discriminator = 111
    }
}