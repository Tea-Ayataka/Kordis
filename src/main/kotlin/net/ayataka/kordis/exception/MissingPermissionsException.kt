package net.ayataka.kordis.exception

import net.ayataka.kordis.entity.server.Server

class MissingPermissionsException(server: Server, target: String) :
        Exception("Server: ${server.name} (${server.id}), $target")