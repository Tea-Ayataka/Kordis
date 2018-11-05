package net.ayataka.kordis.entity.server

import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Nameable

interface Emoji : Nameable, Entity {
    override val name: String
}