package net.ayataka.kordis.entity.collection

import kotlinx.serialization.json.JsonObject
import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Updatable
import java.util.concurrent.ConcurrentHashMap

open class EntitySetImpl<T : Entity> : IterableEntitySet<T>, MutableCollection<T> {
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

    @Deprecated("use add(id: Long, element: () -> T) instead of this")
    override fun add(element: T): Boolean {
        throw UnsupportedOperationException()
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

    fun put(value: T): T {
        synchronized(entities) {
            if (entities[value.id] != null) {
                throw IllegalArgumentException("The entity is already in the set! ($value)")
            }

            entities[value.id] = value
            return value
        }
    }

    fun remove(id: Long) {
        synchronized(entities) {
            if (entities[id] != null) {
                entities.remove(id)
            }
        }
    }

    @Suppress("FoldInitializerAndIfToElvis")
    fun update(id: Long, data: JsonObject): T? {
        synchronized(entities) {
            val entity = entities[id] ?: return null
            if (entity !is Updatable) {
                throw UnsupportedOperationException("The entities are not updatable")
            }

            entity.update(data)
            return entity
        }
    }

    @Suppress("FoldInitializerAndIfToElvis")
    fun updateOrPut(id: Long, data: JsonObject, value: () -> T): T {
        synchronized(entities) {
            val entity = entities[id] ?: return put(value())

            if (entity !is Updatable) {
                throw UnsupportedOperationException("The entities are not updatable")
            }

            entity.update(data)
            return entity
        }
    }

    fun getOrPut(id: Long, value: () -> T): T {
        synchronized(entities) {
            entities[id]?.let { return it }

            val entity = value()
            put(entity)
            return entity
        }
    }
}