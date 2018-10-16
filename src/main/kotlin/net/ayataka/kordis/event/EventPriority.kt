package net.ayataka.kordis.event

enum class EventPriority(val order: Int) {
    HIGHEST(0),
    HIGH(1),
    NORMAL(2),
    LOW(3),
    LOWEST(4),
}