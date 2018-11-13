package net.ayataka.kordis.entity.server.member

import net.ayataka.kordis.entity.collection.find
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.user.User

suspend fun User.unban(server: Server) {
    server.unban(this)
}

suspend fun User.ban(server: Server, deleteMessageDays: Int = 0, reason: String? = null) {
    server.ban(this, deleteMessageDays, reason)
}

suspend fun Member.kick() {
    server.kick(this)
}

suspend fun Member.ban(deleteMessageDays: Int = 0, reason: String? = null) {
    server.ban(this, deleteMessageDays, reason)
}

suspend fun Member.unban() {
    server.unban(this)
}

fun User.member(server: Server) = server.members.find(this)