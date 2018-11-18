package net.ayataka.kordis.entity

import com.google.gson.JsonObject

interface Updatable : Entity {
    fun update(json: JsonObject)
}