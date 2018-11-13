package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.Nameable

open class NameableEntitySetImpl<T : Nameable> : NameableEntitySet<T>, EntitySetImpl<T>() {
    override fun findByQuery(text: String): List<T> {
        val exact = mutableListOf<T>()
        val wrongcase = mutableListOf<T>()
        val startswith = mutableListOf<T>()
        val contains = mutableListOf<T>()

        val lowerQuery = text.toLowerCase()

        this.sortedBy { it.name }.forEach {
            val name = it.name

            when {
                name == text -> exact.add(it)
                name.equals(text, true) && exact.isEmpty() -> wrongcase.add(it)
                name.toLowerCase().startsWith(lowerQuery) && wrongcase.isEmpty() -> startswith.add(it)
                name.toLowerCase().contains(lowerQuery) && startswith.isEmpty() -> contains.add(it)
            }
        }

        return when {
            exact.isNotEmpty() -> exact.toList()
            wrongcase.isNotEmpty() -> wrongcase.toList()
            startswith.isNotEmpty() -> startswith.toList()
            else -> contains.toList()
        }
    }

    override fun findByName(text: String, ignoreCase: Boolean) =
            entities.values.find { it.name.equals(text, ignoreCase) }
}