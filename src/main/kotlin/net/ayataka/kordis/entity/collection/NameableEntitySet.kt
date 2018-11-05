package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Nameable

interface NameableEntitySet<T : Nameable> : EntitySet<T> {
    fun findByName(text: String, ignoreCase: Boolean = false): T?
}
