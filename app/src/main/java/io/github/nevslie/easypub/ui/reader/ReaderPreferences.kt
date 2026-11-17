package io.github.nevslie.easypub.ui.reader

import androidx.compose.ui.graphics.toArgb
import io.github.nevslie.easypub.data.settings.ReaderSettings
import io.github.nevslie.easypub.ui.theme.ReaderDarkBackground
import io.github.nevslie.easypub.ui.theme.ReaderDarkText
import io.github.nevslie.easypub.ui.theme.ReaderLightBackground
import io.github.nevslie.easypub.ui.theme.ReaderLightText
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Color
import org.readium.r2.navigator.preferences.Theme

/**
 * Übersetzt unsere [ReaderSettings] in die Einstellungen, die Readium für den Buchinhalt nutzt.
 */
fun ReaderSettings.toEpubPreferences(): EpubPreferences =
    EpubPreferences(
        // Durchgehendes vertikales Scrollen statt Blättern.
        scroll = true,
        theme = if (darkContent) Theme.DARK else Theme.LIGHT,
        // Eigene Pastellfarben statt der Readium-Standardfarben.
        backgroundColor = Color((if (darkContent) ReaderDarkBackground else ReaderLightBackground).toArgb()),
        textColor = Color((if (darkContent) ReaderDarkText else ReaderLightText).toArgb()),
        fontSize = fontSize,
    )
