package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Entity

interface IterableEntityList<T : Entity> : EntityList<T>, Collection<T>