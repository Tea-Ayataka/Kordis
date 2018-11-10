package net.ayataka.kordis.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.awt.Color
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun Int.seconds() = this.toLong() * 1000
fun Int.minutes() = this.toLong() * 1000 * 60
fun Int.hours() = this.toLong() * 1000 * 60 * 60
fun Int.days() = this.toLong() * 1000 * 60 * 60 * 24

fun Instant.formatAsDate() = SimpleDateFormat("yyyy-MM-dd").format(Date.from(this))!!

fun Color.uRgb() = ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)

fun ByteArray.base64() = Base64.getEncoder().encodeToString(this)!!

suspend fun Call.executeAsync() = suspendCoroutine<Response> {
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            it.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            it.resumeWithException(e)
        }
    })
}