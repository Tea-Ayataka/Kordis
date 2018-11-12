package net.ayataka.kordis.event

import kotlin.reflect.KClass

class Listener<E : Event>(val event: KClass<E>, val action: suspend (E) -> Unit)