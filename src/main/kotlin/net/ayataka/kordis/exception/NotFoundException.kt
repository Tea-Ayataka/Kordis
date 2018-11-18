package net.ayataka.kordis.exception

open class NotFoundException(remote: Boolean = false) : Exception("the entity no longer exists (remote: $remote)")