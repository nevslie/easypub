package io.github.nevslie.easypub.ui.library

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nevslie.easypub.AppContainer
import io.github.nevslie.easypub.R
import io.github.nevslie.easypub.data.db.BookEntity
import io.github.nevslie.easypub.data.db.LibraryEntity
import io.github.nevslie.easypub.domain.BookSearch
import io.github.nevslie.easypub.ui.util.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = true,
    val library: LibraryEntity? = null,
    val hasAccess: Boolean = true,
    val isScanning: Boolean = false,
    /** Alle Bücher der Library, unabhängig von der Suche. */
    val totalBookCount: Int = 0,
    /** Bücher passend zur aktuellen Suche. */
    val books: List<BookEntity> = emptyList(),
    val openingBookId: Long? = null,
)

sealed interface LibraryEvent {
    data class OpenReader(val bookId: Long) : LibraryEvent
    data class ShowMessage(val message: Int) : LibraryEvent
}

class LibraryViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val libraryId: Long = checkNotNull(savedStateHandle[ARG_LIBRARY_ID])

    private val repository = container.libraryRepository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val openingBookId = MutableStateFlow<Long?>(null)

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeLibrary(libraryId),
        repository.observeBooks(libraryId),
        repository.scanningLibraryIds,
        _query,
        openingBookId,
    ) { library, books, scanning, query, opening ->
        LibraryUiState(
            isLoading = false,
            library = library,
            hasAccess = library != null && repository.hasAccess(library),
            isScanning = libraryId in scanning,
            totalBookCount = books.size,
            books = BookSearch.filter(books, query) { listOf(it.title, it.author) },
            openingBookId = opening,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun refresh() {
        container.appScope.launch {
            repository.scan(libraryId).onFailure { _events.send(LibraryEvent.ShowMessage(R.string.message_scan_failed)) }
        }
    }

    /** Ordnet der Library einen neu gewählten Ordner zu, z. B. nachdem der Zugriff verloren ging. */
    fun relink(treeUri: Uri) {
        viewModelScope.launch {
            try {
                repository.relinkLibrary(libraryId, treeUri)
            } catch (e: SecurityException) {
                _events.send(LibraryEvent.ShowMessage(R.string.message_folder_access_failed))
                return@launch
            }
            refresh()
        }
    }

    fun openBook(book: BookEntity) {
        if (openingBookId.value != null) return
        openingBookId.value = book.id
        viewModelScope.launch {
            try {
                // Frisch aus der Datenbank laden, damit die zuletzt gespeicherte Lesestelle genutzt wird.
                val current = repository.getBook(book.id) ?: book
                val settings = container.settingsRepository.currentReaderSettings()
                container.readerSessions.open(current, settings)
                    .onSuccess { _events.send(LibraryEvent.OpenReader(book.id)) }
                    .onFailure { _events.send(LibraryEvent.ShowMessage(R.string.message_book_open_failed)) }
            } finally {
                openingBookId.value = null
            }
        }
    }

    companion object {
        const val ARG_LIBRARY_ID = "libraryId"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { LibraryViewModel(appContainer(), createSavedStateHandle()) }
        }
    }
}
