package net.ayataka.kordis.exception

open class NotFoundException(remote: Boolean = false) : Exception("remote: $remote")