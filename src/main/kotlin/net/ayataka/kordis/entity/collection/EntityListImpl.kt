package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Entity
import java.util.concurrent.ConcurrentHashMap

open class EntityListImpl<T : Entity> : IterableEntityList<T>, MutableCollection<T> {
    protected val entities = ConcurrentHashMap<Long, T>()

    override val size: Int
        get() = entities.size

    override fun contains(element: T): Boolean {
        return entities.values.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return entities.values.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return entities.values.isEmpty()
    }

    override fun iterator(): MutableIterator<T> {
        return entities.values.iterator()
    }

    override fun add(element: T): Boolean {
        if (entities[element.id] != null) {
            return false
        }

        entities[element.id] = element
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var modified = false

        elements.forEach {
            synchronized(entities) {
                if (entities[it.id] == null) {
                    entities[it.id] = it
                    modified = true
                }
            }
        }

        return modified
    }

    override fun clear() {
        entities.clear()
    }

    override fun remove(element: T): Boolean {
        if (entities[element.id] == null) {
            return false
        }

        entities.remove(element.id)
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var modified = false

        synchronized(entities) {
            elements.forEach {
                if (entities[it.id] != null) {
                    entities.remove(it.id)
                    modified = true
                }
            }
        }

        return modified
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var modified = false

        synchronized(entities) {
            entities.forEach { key, value ->
                if (value !in elements) {
                    entities.remove(key)
                    modified = true
                }
            }
        }

        return modified
    }

    override fun find(id: Long): T? {
        return entities[id]
    }
}