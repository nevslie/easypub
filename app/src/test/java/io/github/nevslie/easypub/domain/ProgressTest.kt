package io.github.nevslie.easypub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {

    @Test
    fun `wandelt in ganze Prozent um und rundet ab`() {
        assertEquals(0, Progress.percent(0.0))
        assertEquals(42, Progress.percent(0.429))
        assertEquals(99, Progress.percent(0.999))
        assertEquals(100, Progress.percent(1.0))
    }

    @Test
    fun `ungültige Werte werden abgefangen`() {
        assertEquals(0, Progress.percent(null))
        assertEquals(0, Progress.percent(Double.NaN))
        assertEquals(0, Progress.percent(-0.5))
        assertEquals(100, Progress.percent(1.7))
    }

    @Test
    fun `ein Buch gilt ab 1 Prozent als angefangen`() {
        assertFalse(Progress.isStarted(null))
        assertFalse(Progress.isStarted(0.005))
        assertTrue(Progress.isStarted(0.01))
    }
}
