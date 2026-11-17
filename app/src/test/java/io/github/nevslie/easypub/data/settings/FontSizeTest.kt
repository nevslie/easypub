package io.github.nevslie.easypub.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FontSizeTest {

    @Test
    fun `vergrößern und verkleinern in 10-Prozent-Schritten`() {
        assertEquals(1.1, FontSize.increase(1.0), 0.0)
        assertEquals(0.9, FontSize.decrease(1.0), 0.0)
    }

    @Test
    fun `wiederholtes Vergrößern bleibt exakt`() {
        var size = FontSize.DEFAULT
        repeat(3) { size = FontSize.increase(size) }
        // Ohne Rundung käme hier 1.3000000000000003 heraus.
        assertEquals(1.3, size, 0.0)
    }

    @Test
    fun `bleibt innerhalb der Grenzen`() {
        assertEquals(FontSize.MAX, FontSize.increase(FontSize.MAX), 0.0)
        assertEquals(FontSize.MIN, FontSize.decrease(FontSize.MIN), 0.0)
        assertEquals(FontSize.MAX, FontSize.clamp(10.0), 0.0)
        assertFalse(FontSize.canIncrease(FontSize.MAX))
        assertTrue(FontSize.canDecrease(1.0))
    }

    @Test
    fun `Prozentanzeige`() {
        assertEquals(100, FontSize.percent(1.0))
        assertEquals(120, FontSize.percent(1.2))
    }
}
