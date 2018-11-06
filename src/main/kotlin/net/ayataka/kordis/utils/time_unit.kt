package net.ayataka.kordis.utils

fun Int.seconds() = this.toLong() * 1000
fun Int.minutes() = this.toLong() * 1000 * 60
fun Int.hours() = this.toLong() * 1000 * 60 * 60
fun Int.days() = this.toLong() * 1000 * 60 * 60 * 24