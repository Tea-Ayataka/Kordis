package net.ayataka.kordis.utils

import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

fun Int.seconds() = this.toLong() * 1000
fun Int.minutes() = this.toLong() * 1000 * 60
fun Int.hours() = this.toLong() * 1000 * 60 * 60
fun Int.days() = this.toLong() * 1000 * 60 * 60 * 24

fun Instant.formatAsDate() = SimpleDateFormat("yyyy-MM-dd").format(Date.from(this))!!

fun Color.uRgb() = ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)
