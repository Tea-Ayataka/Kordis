package net.ayataka.kordis.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

fun Int.seconds() = this.toLong() * 1000
fun Int.minutes() = this.toLong() * 1000 * 60
fun Int.hours() = this.toLong() * 1000 * 60 * 60
fun Int.days() = this.toLong() * 1000 * 60 * 60 * 24

fun Instant.formatAsDate() = SimpleDateFormat("yyyy-MM-dd").format(Date.from(this))