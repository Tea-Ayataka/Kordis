package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.user.User

fun permissions(vararg permission: Permission) = PermissionSet().apply { addAll(permission) }

suspend fun User.unban(server: Server) {
    server.unban(this)
}

suspend fun Server.ban(user: User, deleteMessageDays: Int = 0, reason: String? = null) {
    user.ban(this, deleteMessageDays, reason)
}

suspend fun Server.kick(user: User) {
    members.find(id)?.kick() ?: throw IllegalArgumentException("this user is not in the server")
}

suspend fun Member.ban(deleteMessageDays: Int = 0, reason: String? = null) {
    ban(server, deleteMessageDays, reason)
}

suspend fun Member.unban() {
    unban(server)
}

