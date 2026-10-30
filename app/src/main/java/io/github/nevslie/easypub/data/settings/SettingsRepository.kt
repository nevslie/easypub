package io.github.nevslie.easypub.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class AppTheme { SYSTEM, LIGHT, DARK }

/** Einstellungen für den Buchinhalt. Sie gelten global für alle Bücher. */
data class ReaderSettings(
    /** Hell/Dunkel nur für den Buchinhalt, unabhängig vom App-Theme. */
    val darkContent: Boolean = false,
    val fontSize: Double = FontSize.DEFAULT,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    val appTheme: Flow<AppTheme> = dataStore.data.map { prefs ->
        prefs[KEY_APP_THEME]
            ?.let { name -> AppTheme.entries.firstOrNull { it.name == name } }
            ?: AppTheme.SYSTEM
    }

    val readerSettings: Flow<ReaderSettings> = dataStore.data.map { prefs ->
        ReaderSettings(
            darkContent = prefs[KEY_READER_DARK] ?: false,
            fontSize = FontSize.clamp(prefs[KEY_READER_FONT_SIZE] ?: FontSize.DEFAULT),
        )
    }

    suspend fun currentReaderSettings(): ReaderSettings = readerSettings.first()

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { it[KEY_APP_THEME] = theme.name }
    }

    suspend fun setReaderDarkContent(dark: Boolean) {
        dataStore.edit { it[KEY_READER_DARK] = dark }
    }

    suspend fun setReaderFontSize(fontSize: Double) {
        dataStore.edit { it[KEY_READER_FONT_SIZE] = FontSize.clamp(fontSize) }
    }

    private companion object {
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_READER_DARK = booleanPreferencesKey("reader_dark_content")
        val KEY_READER_FONT_SIZE = doublePreferencesKey("reader_font_size")
    }
}
