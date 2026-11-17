package io.github.nevslie.easypub.data.settings

import kotlin.math.roundToInt

/**
 * Schriftgröße im Reader als Faktor (1.0 = 100 %), so wie Readium sie erwartet.
 */
object FontSize {
    const val DEFAULT = 1.0
    const val MIN = 0.7
    const val MAX = 2.5
    const val STEP = 0.1

    fun clamp(value: Double): Double = round(value.coerceIn(MIN, MAX))

    fun increase(value: Double): Double = clamp(value + STEP)

    fun decrease(value: Double): Double = clamp(value - STEP)

    fun canIncrease(value: Double): Boolean = value < MAX

    fun canDecrease(value: Double): Boolean = value > MIN

    /** z. B. `1.2` → `120 %` */
    fun percent(value: Double): Int = (value * 100).roundToInt()

    // Gleitkomma-Ungenauigkeiten durch wiederholtes Addieren vermeiden (0.1 + 0.2 ...).
    private fun round(value: Double): Double = (value * 10).roundToInt() / 10.0
}
