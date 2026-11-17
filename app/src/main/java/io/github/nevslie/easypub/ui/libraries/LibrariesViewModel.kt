package io.github.nevslie.easypub.ui.libraries

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nevslie.easypub.AppContainer
import io.github.nevslie.easypub.R
import io.github.nevslie.easypub.data.db.LibraryEntity
import io.github.nevslie.easypub.data.library.LibraryRepository
import io.github.nevslie.easypub.ui.util.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryTile(
    val library: LibraryEntity,
    val bookCount: Int,
    val hasAccess: Boolean,
    val isScanning: Boolean,
)

data class LibrariesUiState(
    val isLoading: Boolean = true,
    val libraries: List<LibraryTile> = emptyList(),
)

class LibrariesViewModel(private val container: AppContainer) : ViewModel() {

    private val repository: LibraryRepository = container.libraryRepository

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** Einmalige Hinweise für die Snackbar (String-Ressourcen-IDs). */
    val messages = _messages.receiveAsFlow()

    val uiState: StateFlow<LibrariesUiState> =
        combine(repository.observeLibraries(), repository.scanningLibraryIds) { libraries, scanning ->
            LibrariesUiState(
                isLoading = false,
                libraries = libraries.map {
                    LibraryTile(
                        library = it.library,
                        bookCount = it.bookCount,
                        hasAccess = repository.hasAccess(it.library),
                        isScanning = it.library.id in scanning,
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibrariesUiState())

    init {
        // Beim App-Start alle Libraries abgleichen, damit neue oder gelöschte Dateien auftauchen.
        container.appScope.launch { repository.scanAll() }
    }

    fun addLibrary(treeUri: Uri) {
        viewModelScope.launch {
            val result = try {
                repository.addLibrary(treeUri)
            } catch (e: SecurityException) {
                showMessage(R.string.message_folder_access_failed)
                return@launch
            }
            when (result) {
                is LibraryRepository.AddResult.Added -> scanInBackground(result.libraryId)
                is LibraryRepository.AddResult.AlreadyExists -> showMessage(R.string.message_library_exists)
            }
        }
    }

    fun rescan(libraryId: Long) = scanInBackground(libraryId)

    fun removeLibrary(libraryId: Long) {
        viewModelScope.launch {
            repository.removeLibrary(libraryId)
            showMessage(R.string.message_library_removed)
        }
    }

    private fun scanInBackground(libraryId: Long) {
        container.appScope.launch {
            repository.scan(libraryId).onFailure { showMessage(R.string.message_scan_failed) }
        }
    }

    private fun showMessage(@StringRes message: Int) {
        _messages.trySend(message)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { LibrariesViewModel(appContainer()) }
        }
    }
}
