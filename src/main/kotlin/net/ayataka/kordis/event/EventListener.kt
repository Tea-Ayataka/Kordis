package net.ayataka.kordis.event

@Target(AnnotationTarget.FUNCTION)
@SuppressWarnings("unused")
annotation class EventListener(val priority: EventPriority = EventPriority.NORMAL)