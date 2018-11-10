package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User

suspend fun User.unban(server: Server) {
    server.unban(this)
}

suspend fun User.ban(server: Server, deleteMessageDays: Int = 0, reason: String? = null) {
    server.ban(this, deleteMessageDays, reason)
}

suspend fun Member.kick(server: Server) {
    server.kick(this)
}

suspend fun Member.ban(deleteMessageDays: Int = 0, reason: String? = null) {
    server.ban(this, deleteMessageDays, reason)
}

suspend fun Member.unban() {
    server.unban(this)
}

