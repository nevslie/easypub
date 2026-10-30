package io.github.nevslie.easypub.data.readium

import android.net.Uri
import io.github.nevslie.easypub.data.db.BookEntity
import io.github.nevslie.easypub.data.settings.ReaderSettings
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

/**
 * Ein geöffnetes Buch, das gerade im Reader angezeigt wird.
 */
class ReaderSession(
    val book: BookEntity,
    val publication: Publication,
    val initialLocator: Locator?,
    /** Reader-Einstellungen zum Zeitpunkt des Öffnens, damit das Buch direkt richtig aussieht. */
    val initialSettings: ReaderSettings,
)

/**
 * Hält die geöffneten Bücher im Speicher.
 *
 * Das Öffnen passiert, bevor die [io.github.nevslie.easypub.ui.reader.ReaderActivity]
 * startet. Die Activity holt sich die Session dann über die Buch-ID ab. So muss die Activity
 * nicht selbst asynchron laden, bevor sie den Reader anzeigen kann.
 */
class ReaderSessions(private val readium: Readium) {

    private val sessions = mutableMapOf<Long, ReaderSession>()

    @Synchronized
    fun get(bookId: Long): ReaderSession? = sessions[bookId]

    suspend fun open(book: BookEntity, settings: ReaderSettings): Result<ReaderSession> {
        get(book.id)?.let { return Result.success(it) }

        return readium.open(Uri.parse(book.documentUri)).map { publication ->
            val session = ReaderSession(
                book = book,
                publication = publication,
                initialLocator = book.locatorJson?.let(::parseLocator),
                initialSettings = settings,
            )
            synchronized(this) { sessions[book.id] = session }
            session
        }
    }

    @Synchronized
    fun close(bookId: Long) {
        sessions.remove(bookId)?.publication?.close()
    }

    private fun parseLocator(json: String): Locator? =
        try {
            Locator.fromJSON(JSONObject(json))
        } catch (e: Exception) {
            null
        }
}
