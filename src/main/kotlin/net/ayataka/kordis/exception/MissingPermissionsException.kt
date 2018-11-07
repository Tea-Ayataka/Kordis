package net.ayataka.kordis.exception

import net.ayataka.kordis.entity.server.Server

class MissingPermissionsException(server: Server) : Exception("Server: ${server.name} (${server.id})")