package net.ayataka.kordis.entity

import kotlinx.serialization.json.JsonObject

interface Updatable : Entity {
    fun update(json: JsonObject)
}