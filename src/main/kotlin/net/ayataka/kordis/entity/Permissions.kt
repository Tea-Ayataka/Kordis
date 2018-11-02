package net.ayataka.kordis.entity

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Permissions(compiled: Int = 0) {
    private val mutex = Mutex()
    private val perms = mutableSetOf<Permission>()

    init {
        perms.addAll(Permission.values().filter { (compiled and it.bitmask) != 0 })
        println(perms.sortedBy { it.desciption }.joinToString { it.desciption })
    }

    suspend fun add(vararg permission: Permission) {
        mutex.withLock {
            perms.addAll(permission)
        }
    }

    suspend fun remove(vararg permission: Permission) {
        mutex.withLock {
            perms.removeAll(permission)
        }
    }

    suspend fun compile(): Int = mutex.withLock {
        var result = 0
        perms.forEach {
            result = result or it.bitmask
        }
        return result
    }
}

enum class Permission(val bitmask: Int, val desciption: String) {
    CREATE_INSTANT_INVITE(0x00000001, "Create Instant Invite"),
    KICK_MEMBERS(0x00000002, "Kick Members"),
    BAN_MEMBERS(0x00000004, "Ban Members"),
    ADMINISTRATOR(0x00000008, "Administrator"),
    MANAGE_CHANNELS(0x00000010, "Manage Channels"),
    MANAGE_GUILD(0x00000020, "Manage Server"),
    MANAGE_MESSAGES(0x00002000, "Manage Messages"),
    MANAGE_NICKNAMES(0x08000000, "Manage Nicknames"),
    MANAGE_ROLES(0x10000000, "Manage Roles"),
    MANAGE_WEBHOOKS(0x20000000, "Manage Webhooks"),
    MANAGE_EMOJIS(0x40000000, "Manage Emojis"),
    ADD_REACTIONS(0x00000040, "Add Reactions"),
    VIEW_AUDIT_LOG(0x00000080, "View Audit Log"),
    VIEW_CHANNEL(0x00000400, "View Channel"),
    SEND_MESSAGES(0x00000800, "Send Messages"),
    SEND_TTS_MESSAGES(0x00001000, "Send TTS Messages"),
    EMBED_LINKS(0x00004000, "Embed Links"),
    ATTACH_FILES(0x00008000, "Attach Files"),
    READ_MESSAGE_HISTORY(0x00010000, "Read Message History"),
    MENTION_EVERYONE(0x00020000, "Mention Everyone"),
    USE_EXTERNAL_EMOJIS(0x00040000, "Use External Emojis"),
    CONNECT(0x00100000, "Connect"),
    SPEAK(0x00200000, "Speak"),
    MUTE_MEMBERS(0x00400000, "Mute Members"),
    DEAFEN_MEMBERS(0x00800000, "Deafen Members"),
    MOVE_MEMBERS(0x01000000 , "Move Members"),
    USE_VAD(0x02000000, "Use VAD"),
    PRIORITY_SPEAKER(0x00000100, "Priority Speaker"),
    CHANGE_NICKNAME(0x04000000, "Change Nickname"),
}

fun main(args: Array<String>) = runBlocking {
    println(Permissions(8).compile())
}