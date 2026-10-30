package io.github.nevslie.easypub.data.library

/**
 * Hilfsfunktionen rund um Dateinamen und Pfade beim Scannen einer Library.
 */
object EpubFiles {

    const val EPUB_MIME_TYPE = "application/epub+zip"

    fun isEpub(fileName: String, mimeType: String?): Boolean =
        mimeType == EPUB_MIME_TYPE || fileName.endsWith(".epub", ignoreCase = true)

    /** Versteckte Dateien und Ordner (z. B. `.trash`) werden beim Scan übersprungen. */
    fun isHidden(fileName: String): Boolean = fileName.startsWith(".")

    fun childPath(parentPath: String, name: String): String =
        if (parentPath.isEmpty()) name else "$parentPath/$name"

    /** Fallback-Titel, wenn das EPUB keinen Titel enthält: `Der_Hobbit.epub` → `Der Hobbit`. */
    fun titleFromFileName(fileName: String): String =
        fileName
            .removeSuffix(".epub")
            .removeSuffix(".EPUB")
            .replace('_', ' ')
            .trim()
            .ifEmpty { fileName }
}
