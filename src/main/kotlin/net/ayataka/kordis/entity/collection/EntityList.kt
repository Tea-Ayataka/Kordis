package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Entity

interface EntityList<T : Entity> {
    fun find(id: Long): T?
}