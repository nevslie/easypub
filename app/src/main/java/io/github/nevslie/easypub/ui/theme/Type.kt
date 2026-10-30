package io.github.nevslie.easypub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Default = Typography()

/**
 * Ruhige Typografie: Systemschrift, große fette Seitentitel wie bei iOS,
 * sonst eher leichte Gewichte.
 */
val EasyPubTypography = Typography(
    displaySmall = Default.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = Default.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = Default.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Default.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Default.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Default.bodyLarge,
    bodyMedium = Default.bodyMedium,
    bodySmall = Default.bodySmall,
    labelLarge = Default.labelLarge.copy(fontWeight = FontWeight.Medium),
    labelMedium = Default.labelMedium,
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
)
