package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Nameable

interface NameableIterableEntitySet<T : Nameable> : NameableEntitySet<T>, IterableEntitySet<T>