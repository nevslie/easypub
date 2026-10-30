package io.github.nevslie.easypub

import android.content.Context
import io.github.nevslie.easypub.data.db.AppDatabase
import io.github.nevslie.easypub.data.library.LibraryRepository
import io.github.nevslie.easypub.data.library.LibraryScanner
import io.github.nevslie.easypub.data.readium.Readium
import io.github.nevslie.easypub.data.readium.ReaderSessions
import io.github.nevslie.easypub.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Einfache manuelle Dependency Injection: Hier werden alle app-weiten Objekte einmal erzeugt.
 * ViewModels und Activities holen sich ihre Abhängigkeiten von hier.
 */
class AppContainer(context: Context) {

    /** Scope für Arbeit, die nicht abbrechen soll, wenn ein Screen geschlossen wird (z. B. Speichern). */
    val appScope = CoroutineScope(SupervisorJob())

    private val database = AppDatabase.create(context)

    private val readium = Readium(context)

    val settingsRepository = SettingsRepository(context)

    val libraryRepository = LibraryRepository(
        contentResolver = context.contentResolver,
        libraryDao = database.libraryDao(),
        bookDao = database.bookDao(),
        scanner = LibraryScanner(context, database.bookDao(), readium),
    )

    val readerSessions = ReaderSessions(readium)
}
