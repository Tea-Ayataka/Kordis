package net.ayataka.kordis.entity

class Permissions(compiled: Int = 0) : MutableCollection<Permission> {
    private val perms = HashSet<Permission>(Permission.values().filter { (compiled and it.bitmask) != 0 })

    override val size = perms.size
    override fun contains(element: Permission) = perms.contains(element)
    override fun containsAll(elements: Collection<Permission>) = perms.containsAll(elements)
    override fun isEmpty() = perms.isEmpty()
    override fun add(element: Permission) = perms.add(element)
    override fun addAll(elements: Collection<Permission>) = perms.addAll(elements)
    override fun clear() = perms.clear()
    override fun iterator() = perms.iterator()
    override fun remove(element: Permission) = perms.remove(element)
    override fun removeAll(elements: Collection<Permission>) = perms.removeAll(elements)
    override fun retainAll(elements: Collection<Permission>) = perms.retainAll(elements)

    fun compile(): Int {
        var result = 0
        perms.forEach {
            result = result or it.bitmask
        }
        return result
    }
}

enum class Permission(val bitmask: Int, val desciption: String) {
    // General Permissions
    ADMINISTRATOR(0x00000008, "Administrator"),
    VIEW_AUDIT_LOG(0x00000080, "View Audit Log"),
    MANAGE_GUILD(0x00000020, "Manage Server"),
    MANAGE_ROLES(0x10000000, "Manage Roles"),
    MANAGE_CHANNELS(0x00000010, "Manage Channels"),
    KICK_MEMBERS(0x00000002, "Kick Members"),
    BAN_MEMBERS(0x00000004, "Ban Members"),
    CREATE_INSTANT_INVITE(0x00000001, "Create Instant Invite"),
    CHANGE_NICKNAME(0x04000000, "Change Nickname"),
    MANAGE_NICKNAMES(0x08000000, "Manage Nicknames"),
    MANAGE_EMOJIS(0x40000000, "Manage Emojis"),
    MANAGE_WEBHOOKS(0x20000000, "Manage Webhooks"),
    VIEW_CHANNEL(0x00000400, "View Channel"),

    // Text Permissions
    SEND_MESSAGES(0x00000800, "Send Messages"),
    SEND_TTS_MESSAGES(0x00001000, "Send TTS Messages"),
    MANAGE_MESSAGES(0x00002000, "Manage Messages"),
    EMBED_LINKS(0x00004000, "Embed Links"),
    ATTACH_FILES(0x00008000, "Attach Files"),
    READ_MESSAGE_HISTORY(0x00010000, "Read Message History"),
    MENTION_EVERYONE(0x00020000, "Mention Everyone"),
    USE_EXTERNAL_EMOJIS(0x00040000, "Use External Emojis"),
    ADD_REACTIONS(0x00000040, "Add Reactions"),

   // Voice Permissions
    CONNECT(0x00100000, "Connect"),
    SPEAK(0x00200000, "Speak"),
    MUTE_MEMBERS(0x00400000, "Mute Members"),
    DEAFEN_MEMBERS(0x00800000, "Deafen Members"),
    MOVE_MEMBERS(0x01000000, "Move Members"),
    USE_VAD(0x02000000, "Use Voice Activity"),
    PRIORITY_SPEAKER(0x00000100, "Priority Speaker"),
}