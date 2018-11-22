package net.ayataka.kordis.exception

import net.ayataka.kordis.entity.server.Server

class MissingPermissionsException(message: String) : Exception(message) {
    constructor(server: Server, target: String) : this("Server: ${server.name} (${server.id}), $target")
}