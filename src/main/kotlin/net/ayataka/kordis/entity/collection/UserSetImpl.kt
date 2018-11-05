package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.user.User

class UserSetImpl : UserSet, NameableEntitySetImpl<User>() {
    override fun findByTag(tag: String, ignoreCase: Boolean): User? {
        return entities.values.find { it.tag.equals(tag, ignoreCase) }
    }
}