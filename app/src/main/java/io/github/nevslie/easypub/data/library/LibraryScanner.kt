package io.github.nevslie.easypub.data.library

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import android.util.Size
import io.github.nevslie.easypub.data.db.BookDao
import io.github.nevslie.easypub.data.db.BookEntity
import io.github.nevslie.easypub.data.db.LibraryEntity
import io.github.nevslie.easypub.data.readium.Readium
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.services.coverFitting
import java.io.File

/**
 * Durchsucht den Ordner einer Library rekursiv nach EPUBs und gleicht den Index ab:
 * neue Bücher werden aufgenommen, geänderte neu eingelesen und gelöschte entfernt.
 *
 * Für die Ordnerstruktur werden die `DocumentsContract`-Abfragen direkt verwendet, weil sie
 * bei großen Ordnern deutlich schneller sind als `DocumentFile.listFiles()`.
 */
class LibraryScanner(
    private val context: Context,
    private val bookDao: BookDao,
    private val readium: Readium,
) {

    private val contentResolver: ContentResolver = context.contentResolver

    private val coverDir: File
        get() = File(context.filesDir, "covers").apply { mkdirs() }

    suspend fun scan(library: LibraryEntity) = withContext(Dispatchers.IO) {
        val treeUri = Uri.parse(library.treeUri)
        val found = listEpubs(treeUri)
        val existing = bookDao.getByLibrary(library.id).associateBy { it.relativePath }

        for (file in found) {
            val known = existing[file.relativePath]
            when {
                known == null -> insertBook(library.id, file)
                known.fileSize != file.size || known.lastModified != file.lastModified ->
                    updateBook(known, file)
                known.documentUri != file.uri.toString() ->
                    // Ordner wurde neu ausgewählt: Datei unverändert, nur die URI ist neu.
                    bookDao.update(known.copy(documentUri = file.uri.toString()))
            }
        }

        val foundPaths = found.mapTo(HashSet()) { it.relativePath }
        val removed = existing.values.filter { it.relativePath !in foundPaths }
        if (removed.isNotEmpty()) {
            removed.forEach { book -> book.coverPath?.let { File(it).delete() } }
            bookDao.delete(removed.map { it.id })
        }
    }

    /** Löscht alle extrahierten Cover der angegebenen Bücher. */
    fun deleteCovers(books: List<BookEntity>) {
        books.forEach { book -> book.coverPath?.let { File(it).delete() } }
    }

    private suspend fun insertBook(libraryId: Long, file: EpubFile) {
        val metadata = readMetadata(file)
        val id = bookDao.insert(
            BookEntity(
                libraryId = libraryId,
                documentUri = file.uri.toString(),
                relativePath = file.relativePath,
                title = metadata.title,
                author = metadata.author,
                identifier = metadata.identifier,
                coverPath = null,
                fileSize = file.size,
                lastModified = file.lastModified,
                addedAt = System.currentTimeMillis(),
            )
        )
        val coverPath = metadata.cover?.let { saveCover(id, it) }
        if (coverPath != null) {
            bookDao.get(id)?.let { bookDao.update(it.copy(coverPath = coverPath)) }
        }
    }

    private suspend fun updateBook(book: BookEntity, file: EpubFile) {
        val metadata = readMetadata(file)
        val coverPath = metadata.cover?.let { saveCover(book.id, it) }
        bookDao.update(
            book.copy(
                documentUri = file.uri.toString(),
                title = metadata.title,
                author = metadata.author,
                identifier = metadata.identifier,
                coverPath = coverPath,
                fileSize = file.size,
                lastModified = file.lastModified,
            )
        )
    }

    private suspend fun readMetadata(file: EpubFile): Metadata {
        val fallbackTitle = EpubFiles.titleFromFileName(file.name)
        val publication = readium.open(file.uri).getOrElse { error ->
            Log.w(TAG, "EPUB konnte nicht gelesen werden: ${file.relativePath}", error)
            return Metadata(title = fallbackTitle, author = null, identifier = null, cover = null)
        }
        return try {
            val metadata = publication.metadata
            Metadata(
                title = metadata.title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
                author = metadata.authors
                    .map { it.name }
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifBlank { null },
                identifier = metadata.identifier,
                cover = publication.coverFitting(COVER_SIZE),
            )
        } finally {
            publication.close()
        }
    }

    private fun saveCover(bookId: Long, bitmap: Bitmap): String? =
        try {
            val file = File(coverDir, "$bookId.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            file.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Cover konnte nicht gespeichert werden", e)
            null
        }

    /** Listet alle EPUBs im Ordner und seinen Unterordnern. */
    private fun listEpubs(treeUri: Uri): List<EpubFile> {
        val result = mutableListOf<EpubFile>()
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        // Ordner werden iterativ statt rekursiv abgearbeitet, damit tiefe Strukturen kein Problem sind.
        val pending = ArrayDeque<Pair<String, String>>()
        pending.add(rootId to "")

        while (pending.isNotEmpty()) {
            val (documentId, path) = pending.removeFirst()
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
            contentResolver.query(childrenUri, CHILD_COLUMNS, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childId = cursor.getString(0)
                    val name = cursor.getString(1) ?: continue
                    val mimeType = cursor.getString(2)
                    if (EpubFiles.isHidden(name)) continue

                    val childPath = EpubFiles.childPath(path, name)
                    if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        pending.add(childId to childPath)
                    } else if (EpubFiles.isEpub(name, mimeType)) {
                        result += EpubFile(
                            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId),
                            name = name,
                            relativePath = childPath,
                            size = if (cursor.isNull(3)) 0 else cursor.getLong(3),
                            lastModified = if (cursor.isNull(4)) 0 else cursor.getLong(4),
                        )
                    }
                }
            }
        }
        return result
    }

    private data class EpubFile(
        val uri: Uri,
        val name: String,
        val relativePath: String,
        val size: Long,
        val lastModified: Long,
    )

    private data class Metadata(
        val title: String,
        val author: String?,
        val identifier: String?,
        val cover: Bitmap?,
    )

    private companion object {
        const val TAG = "LibraryScanner"
        val COVER_SIZE = Size(480, 720)
        val CHILD_COLUMNS = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}
