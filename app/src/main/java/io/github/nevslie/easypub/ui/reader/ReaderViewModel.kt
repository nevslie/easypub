package io.github.nevslie.easypub.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nevslie.easypub.AppContainer
import io.github.nevslie.easypub.data.settings.FontSize
import io.github.nevslie.easypub.data.settings.ReaderSettings
import io.github.nevslie.easypub.ui.util.appContainer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Locator

@OptIn(FlowPreview::class)
class ReaderViewModel(
    private val container: AppContainer,
    private val bookId: Long,
    initialSettings: ReaderSettings,
) : ViewModel() {

    private val settingsRepository = container.settingsRepository

    val settings: StateFlow<ReaderSettings> = settingsRepository.readerSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialSettings)

    private val currentLocator = MutableStateFlow<Locator?>(null)

    init {
        // Beim Scrollen kommen sehr viele Positionen herein. Gespeichert wird erst,
        // wenn die Position kurz stabil ist.
        viewModelScope.launch {
            currentLocator.filterNotNull().debounce(1_000).collect { save(it) }
        }
    }

    fun onLocatorChanged(locator: Locator) {
        currentLocator.value = locator
    }

    /** Speichert die aktuelle Position sofort, z. B. wenn die App in den Hintergrund geht. */
    fun saveNow() {
        val locator = currentLocator.value ?: return
        container.appScope.launch { save(locator) }
    }

    fun toggleDarkContent() {
        viewModelScope.launch { settingsRepository.setReaderDarkContent(!settings.value.darkContent) }
    }

    fun increaseFontSize() {
        viewModelScope.launch { settingsRepository.setReaderFontSize(FontSize.increase(settings.value.fontSize)) }
    }

    fun decreaseFontSize() {
        viewModelScope.launch { settingsRepository.setReaderFontSize(FontSize.decrease(settings.value.fontSize)) }
    }

    private suspend fun save(locator: Locator) {
        container.libraryRepository.saveProgress(
            bookId = bookId,
            locatorJson = locator.toJSON().toString(),
            progression = locator.locations.totalProgression,
        )
    }

    companion object {
        fun factory(bookId: Long, initialSettings: ReaderSettings): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ReaderViewModel(appContainer(), bookId, initialSettings) }
            }
    }
}
