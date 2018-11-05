package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Entity

interface EntitySet<T : Entity> {
    fun find(id: Long): T?
}