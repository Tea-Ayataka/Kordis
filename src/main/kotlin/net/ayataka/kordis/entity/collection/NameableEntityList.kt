package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Nameable

interface NameableEntityList<T : Nameable> : EntityList<T> {
    fun findByName(text: String, ignoreCase: Boolean = false): T?
}
