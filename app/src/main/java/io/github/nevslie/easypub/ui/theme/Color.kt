package io.github.nevslie.easypub.ui.theme

import androidx.compose.ui.graphics.Color

// Pastell-Palette. Akzent ist ein sanftes Lavendel, ergänzt durch Mint und Pfirsich.

// Hell
val LightBackground = Color(0xFFFAF8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2EFF6)
val LightOnSurface = Color(0xFF2B2830)
val LightOnSurfaceVariant = Color(0xFF6B6672)
val LightOutline = Color(0xFFD9D4DF)
val LightPrimary = Color(0xFF7C6FB0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE8E1F7)
val LightOnPrimaryContainer = Color(0xFF2E2445)
val LightSecondary = Color(0xFF5E9A80)
val LightSecondaryContainer = Color(0xFFD9F0E5)
val LightOnSecondaryContainer = Color(0xFF1C3A2D)
val LightTertiary = Color(0xFFC0806A)
val LightTertiaryContainer = Color(0xFFFBE3D8)
val LightOnTertiaryContainer = Color(0xFF45261B)
val LightError = Color(0xFFC25B6A)
val LightErrorContainer = Color(0xFFFADDE2)

// Dunkel (angelehnt an Obsidian)
val DarkBackground = Color(0xFF1C1B20)
val DarkSurface = Color(0xFF232228)
val DarkSurfaceVariant = Color(0xFF2C2A33)
val DarkOnSurface = Color(0xFFE8E4EE)
val DarkOnSurfaceVariant = Color(0xFFA9A3B2)
val DarkOutline = Color(0xFF3D3A45)
val DarkPrimary = Color(0xFFC9B8F2)
val DarkOnPrimary = Color(0xFF2E2445)
val DarkPrimaryContainer = Color(0xFF453A63)
val DarkOnPrimaryContainer = Color(0xFFE8E1F7)
val DarkSecondary = Color(0xFFA8DCC3)
val DarkSecondaryContainer = Color(0xFF2F4A3E)
val DarkOnSecondaryContainer = Color(0xFFD9F0E5)
val DarkTertiary = Color(0xFFF2BFA8)
val DarkTertiaryContainer = Color(0xFF5A3A2E)
val DarkOnTertiaryContainer = Color(0xFFFBE3D8)
val DarkError = Color(0xFFF2A7B3)
val DarkErrorContainer = Color(0xFF5C2B34)

/**
 * Pastellfarben für Library-Kacheln und Cover-Platzhalter.
 * Jede Library bekommt anhand ihrer ID eine feste Farbe.
 */
data class AccentColor(val light: Color, val dark: Color)

val AccentColors = listOf(
    AccentColor(light = Color(0xFFE8E1F7), dark = Color(0xFF3B3352)), // Lavendel
    AccentColor(light = Color(0xFFD9F0E5), dark = Color(0xFF2B4239)), // Mint
    AccentColor(light = Color(0xFFFBE3D8), dark = Color(0xFF4C342B)), // Pfirsich
    AccentColor(light = Color(0xFFDCEBFA), dark = Color(0xFF2B3A4C)), // Himmelblau
    AccentColor(light = Color(0xFFF8DCE6), dark = Color(0xFF4A2F3A)), // Rosé
    AccentColor(light = Color(0xFFF7EFD2), dark = Color(0xFF47402A)), // Vanille
)

// Farben für den Buchinhalt im Reader
val ReaderLightBackground = Color(0xFFFBF8F3)
val ReaderLightText = Color(0xFF2B2830)
val ReaderDarkBackground = Color(0xFF1E1D22)
val ReaderDarkText = Color(0xFFDAD5E2)
