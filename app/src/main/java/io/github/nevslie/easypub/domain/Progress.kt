package io.github.nevslie.easypub.domain

import kotlin.math.floor

object Progress {

    /**
     * Wandelt einen Fortschritt von 0.0 bis 1.0 in ganze Prozent um.
     *
     * Es wird abgerundet, damit ein Buch erst bei wirklich 100 % als fertig angezeigt wird.
     * Ungültige Werte (null, NaN, außerhalb 0..1) werden abgefangen.
     */
    fun percent(progression: Double?): Int {
        if (progression == null || progression.isNaN()) return 0
        return floor(progression.coerceIn(0.0, 1.0) * 100).toInt()
    }

    /** Ob ein Buch schon angefangen wurde und deshalb ein Fortschritt angezeigt werden soll. */
    fun isStarted(progression: Double?): Boolean = percent(progression) > 0
}
