package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.user.User

interface UserSet : NameableEntitySet<User> {
    fun findByTag(tag: String, ignoreCase: Boolean = false): User?
}