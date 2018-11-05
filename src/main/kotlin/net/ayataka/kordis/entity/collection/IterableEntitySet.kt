package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Entity

interface IterableEntitySet<T : Entity> : EntitySet<T>, Collection<T>