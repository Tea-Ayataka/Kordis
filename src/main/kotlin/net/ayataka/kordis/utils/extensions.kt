package net.ayataka.kordis.utils

import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

fun Instant.formatAsDate() = SimpleDateFormat("yyyy-MM-dd").format(Date.from(this))!!

fun Color.uRgb() = ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)

fun ByteArray.base64() = Base64.getEncoder().encodeToString(this)!!