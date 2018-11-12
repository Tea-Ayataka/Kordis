package net.ayataka.kordis.event

@Target(AnnotationTarget.FUNCTION)
annotation class EventHandler(val priority: EventPriority = EventPriority.NORMAL)