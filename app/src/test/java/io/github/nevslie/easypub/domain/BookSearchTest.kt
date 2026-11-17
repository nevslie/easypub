package io.github.nevslie.easypub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BookSearchTest {

    private data class Book(val title: String, val author: String?)

    private val books = listOf(
        Book("Der Hobbit", "J. R. R. Tolkien"),
        Book("Die Verwandlung", "Franz Kafka"),
        Book("Café am Rande der Welt", "John Strelecky"),
        Book("Ohne Autor", null),
    )

    private fun search(query: String) = BookSearch.filter(books, query) { listOf(it.title, it.author) }

    @Test
    fun `leere Suche liefert alle Bücher`() {
        assertEquals(books, search(""))
        assertEquals(books, search("   "))
    }

    @Test
    fun `Groß- und Kleinschreibung wird ignoriert`() {
        assertEquals(listOf(books[0]), search("HOBBIT"))
    }

    @Test
    fun `findet auch über den Autor`() {
        assertEquals(listOf(books[1]), search("kafka"))
    }

    @Test
    fun `alle Wörter müssen vorkommen, Reihenfolge egal`() {
        assertEquals(listOf(books[0]), search("tolkien hobbit"))
        assertEquals(emptyList<Book>(), search("tolkien kafka"))
    }

    @Test
    fun `Akzente werden ignoriert`() {
        assertEquals(listOf(books[2]), search("cafe"))
        assertEquals(listOf(books[2]), search("CAFÉ"))
    }

    @Test
    fun `Bücher ohne Autor stören die Suche nicht`() {
        assertEquals(listOf(books[3]), search("ohne"))
    }
}
