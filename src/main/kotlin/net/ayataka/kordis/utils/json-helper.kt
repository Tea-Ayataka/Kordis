package net.ayataka.kordis.utils

import com.google.gson.*

fun JsonObject.getOrNull(memberName: String): JsonElement? = if (has(memberName) && !get(memberName).isJsonNull) get(memberName) else null

fun JsonObject.getObjectOrNull(memberName: String): JsonObject? {
    if (!has(memberName)) {
        return null
    }

    val member = get(memberName)
    return if (member.isJsonObject) member.asJsonObject else null
}

fun JsonObject.getArrayOrNull(memberName: String): JsonArray? {
    if (!has(memberName)) {
        return null
    }

    val member = get(memberName)
    return if (member.isJsonArray) member.asJsonArray else null
}

val JsonElement.asStringOrNull
    get() = if (isJsonPrimitive && asJsonPrimitive.isString) asString else null

val JsonElement.asLongOrNull
    get() = if (isJsonPrimitive) asLong else null

fun JsonObject.isNotEmpty() = size() > 0

fun json(builder: JsonBuilder.() -> Unit) = JsonBuilder().apply(builder).get()

fun jsonArray(builder: JsonArrayBuilder.() -> Unit) = JsonArrayBuilder().apply(builder).get()

class JsonBuilder {
    private val jsonObject = JsonObject()

    infix fun String.to(value: Number?) {
        jsonObject.addProperty(this, value)
    }

    infix fun String.to(value: String?) {
        jsonObject.addProperty(this, value)
    }

    infix fun String.to(value: Boolean?) {
        jsonObject.addProperty(this, value)
    }

    infix fun String.to(value: JsonElement?) {
        jsonObject.add(this, value)
    }

    fun String.toNull() {
        jsonObject.add(this, JsonNull.INSTANCE)
    }

    fun get() = jsonObject
}

class JsonArrayBuilder {
    private val jsonArray = JsonArray()

    operator fun String?.unaryPlus() {
        jsonArray.add(this)
    }

    operator fun Boolean?.unaryPlus() {
        jsonArray.add(this)
    }

    operator fun JsonElement?.unaryPlus() {
        jsonArray.add(this)
    }

    fun get() = jsonArray
}

internal fun main() {
    println(json {
        "isKid" to true
        "Age" to 10
        "Name" to "John Smith"
        "vitality" to json {
            "status" to "good"
            "sick" to json {
                "isCritical" to false
                "name" to "cold"
            }
        }
        "ids" to jsonArray {
            +JsonPrimitive(1111)
            +JsonPrimitive(222222)
            +JsonPrimitive(333333)

            +"Hello World"
        }
        "jobs" to jsonArray {
            +json {
                "name" to "none"
                "isGood" to false
            }
            +json {
                "name" to "none"
                "isGood" to false
            }
            +json {
                "name" to "none"
                "isGood" to false
            }
        }
    }.toString())
}