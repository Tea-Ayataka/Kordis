package net.ayataka.kordis.utils

import kotlinx.coroutines.channels.ReceiveChannel
import java.awt.Color
import java.util.*

internal fun Color.uRgb() = ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)

internal fun ByteArray.base64() = Base64.getEncoder().encodeToString(this)!!

@Suppress("EXPERIMENTAL_API_USAGE")
internal fun <E> ReceiveChannel<E>.clear() {
    while (!isEmpty) {
        poll()
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
internal suspend fun <E> ReceiveChannel<E>.receiveAll(): List<E> {
    if (isEmpty) {
        return emptyList()
    }

    val result = mutableListOf<E>()
    while (!isEmpty) {
        result.add(receive())
    }
    return result
}