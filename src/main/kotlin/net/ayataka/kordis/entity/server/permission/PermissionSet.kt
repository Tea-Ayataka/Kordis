package net.ayataka.kordis.entity.server.permission

class PermissionSet(compiled: Int = 0) : MutableCollection<Permission> {
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

    override fun toString(): String {
        return "PermissionSet(${perms.joinToString()})"
    }
}

fun permissions(vararg permission: Permission) = PermissionSet().apply { addAll(permission) }