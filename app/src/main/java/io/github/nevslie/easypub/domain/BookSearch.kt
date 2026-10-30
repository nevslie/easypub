package io.github.nevslie.easypub.domain

import java.text.Normalizer
import java.util.Locale

/**
 * Einfache Suche über Titel und Autor.
 *
 * - Groß-/Kleinschreibung und Akzente werden ignoriert („Müller“ findet „muller“).
 * - Jedes Wort der Suchanfrage muss vorkommen, die Reihenfolge ist egal
 *   („tolkien hobbit“ findet „Der Hobbit“ von J. R. R. Tolkien).
 */
object BookSearch {

    fun <T> filter(items: List<T>, query: String, fields: (T) -> List<String?>): List<T> {
        val tokens = tokenize(query)
        if (tokens.isEmpty()) return items
        return items.filter { item ->
            val haystack = fields(item).filterNotNull().joinToString(" ") { normalize(it) }
            tokens.all { haystack.contains(it) }
        }
    }

    fun tokenize(query: String): List<String> =
        normalize(query).split(Regex("\\s+")).filter { it.isNotBlank() }

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
}
