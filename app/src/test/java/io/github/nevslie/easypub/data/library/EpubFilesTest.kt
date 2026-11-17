package io.github.nevslie.easypub.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpubFilesTest {

    @Test
    fun `erkennt EPUBs am Dateinamen oder MIME-Typ`() {
        assertTrue(EpubFiles.isEpub("buch.epub", null))
        assertTrue(EpubFiles.isEpub("BUCH.EPUB", "application/octet-stream"))
        assertTrue(EpubFiles.isEpub("ohne-endung", "application/epub+zip"))
        assertFalse(EpubFiles.isEpub("buch.pdf", "application/pdf"))
    }

    @Test
    fun `versteckte Dateien`() {
        assertTrue(EpubFiles.isHidden(".trash"))
        assertFalse(EpubFiles.isHidden("Fantasy"))
    }

    @Test
    fun `baut relative Pfade`() {
        assertEquals("buch.epub", EpubFiles.childPath("", "buch.epub"))
        assertEquals("Fantasy/buch.epub", EpubFiles.childPath("Fantasy", "buch.epub"))
    }

    @Test
    fun `Titel aus dem Dateinamen`() {
        assertEquals("Der Hobbit", EpubFiles.titleFromFileName("Der_Hobbit.epub"))
        assertEquals("Kafka", EpubFiles.titleFromFileName("Kafka.EPUB"))
        assertEquals(".epub", EpubFiles.titleFromFileName(".epub"))
    }
}
