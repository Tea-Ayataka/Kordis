package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Nameable

open class NameableEntitySetImpl<T : Nameable> : NameableEntitySet<T>, EntitySetImpl<T>() {
    override fun findByName(text: String, ignoreCase: Boolean) =
            entities.values.find { it.name.equals(text, ignoreCase) }
}