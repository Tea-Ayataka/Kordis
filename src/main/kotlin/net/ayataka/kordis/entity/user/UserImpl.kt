package net.ayataka.kordis.entity.user

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.DiscordClient

@Suppress("JoinDeclarationAndAssignment")
class UserImpl(json: JsonObject, override val client: DiscordClient) : User {
    override val id get() = _id
    override val name get() = _name
    override val discriminator get() = _discriminator

    private var _id = -1L
    private var _name = ""
    private var _discriminator = -1

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        _id = json["id"].primitive.long
        _name = "test"
        _discriminator = 111
    }
}