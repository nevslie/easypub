package io.github.nevslie.easypub.data.library

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import io.github.nevslie.easypub.data.db.BookDao
import io.github.nevslie.easypub.data.db.BookEntity
import io.github.nevslie.easypub.data.db.LibraryDao
import io.github.nevslie.easypub.data.db.LibraryEntity
import io.github.nevslie.easypub.data.db.LibraryWithCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val contentResolver: ContentResolver,
    private val libraryDao: LibraryDao,
    private val bookDao: BookDao,
    private val scanner: LibraryScanner,
) {

    sealed interface AddResult {
        data class Added(val libraryId: Long) : AddResult
        data class AlreadyExists(val libraryId: Long) : AddResult
    }

    private val scanMutex = Mutex()

    private val _scanningLibraryIds = MutableStateFlow<Set<Long>>(emptySet())

    /** IDs der Libraries, die gerade gescannt werden. */
    val scanningLibraryIds: StateFlow<Set<Long>> = _scanningLibraryIds.asStateFlow()

    fun observeLibraries(): Flow<List<LibraryWithCount>> = libraryDao.observeAllWithCount()

    fun observeLibrary(id: Long): Flow<LibraryEntity?> = libraryDao.observe(id)

    fun observeBooks(libraryId: Long): Flow<List<BookEntity>> = bookDao.observeByLibrary(libraryId)

    suspend fun getBook(id: Long): BookEntity? = bookDao.get(id)

    /**
     * Registriert einen im Ordner-Explorer gewählten Ordner als Library und
     * sichert den Zugriff darauf dauerhaft (auch nach einem Neustart).
     */
    suspend fun addLibrary(treeUri: Uri): AddResult {
        takePermission(treeUri)
        libraryDao.findByTreeUri(treeUri.toString())?.let { return AddResult.AlreadyExists(it.id) }

        val id = libraryDao.insert(
            LibraryEntity(
                name = folderName(treeUri),
                treeUri = treeUri.toString(),
                addedAt = System.currentTimeMillis(),
            )
        )
        return AddResult.Added(id)
    }

    /**
     * Ordnet einer Library einen neu gewählten Ordner zu, z. B. wenn der Zugriff verloren ging.
     * Bücher mit gleichem relativen Pfad behalten dabei ihren Lesefortschritt.
     */
    suspend fun relinkLibrary(libraryId: Long, treeUri: Uri) {
        val library = libraryDao.get(libraryId) ?: return
        takePermission(treeUri)
        if (library.treeUri != treeUri.toString()) {
            releasePermission(Uri.parse(library.treeUri))
        }
        libraryDao.updateFolder(libraryId, treeUri.toString(), folderName(treeUri))
    }

    /**
     * Entfernt nur die Registrierung der Library. Die Dateien im Ordner bleiben unangetastet.
     */
    suspend fun removeLibrary(libraryId: Long) {
        val library = libraryDao.get(libraryId) ?: return
        scanner.deleteCovers(bookDao.getByLibrary(libraryId))
        libraryDao.delete(libraryId)
        releasePermission(Uri.parse(library.treeUri))
    }

    /** Ob die App (noch) Zugriff auf den Ordner der Library hat. */
    fun hasAccess(library: LibraryEntity): Boolean {
        val uri = Uri.parse(library.treeUri)
        return contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
    }

    suspend fun scan(libraryId: Long): Result<Unit> {
        val library = libraryDao.get(libraryId) ?: return Result.success(Unit)
        if (!hasAccess(library)) return Result.failure(SecurityException("Kein Zugriff auf ${library.name}"))

        _scanningLibraryIds.update { it + libraryId }
        return try {
            // Scans nacheinander ausführen, damit sich zwei Scans derselben Library nicht in die Quere kommen.
            scanMutex.withLock { scanner.scan(library) }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Scan von ${library.name} fehlgeschlagen", e)
            Result.failure(e)
        } finally {
            _scanningLibraryIds.update { it - libraryId }
        }
    }

    suspend fun scanAll() {
        libraryDao.getAll().forEach { scan(it.id) }
    }

    suspend fun saveProgress(bookId: Long, locatorJson: String, progression: Double?) {
        bookDao.updateProgress(bookId, locatorJson, progression, System.currentTimeMillis())
    }

    private fun takePermission(treeUri: Uri) {
        contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun releasePermission(treeUri: Uri) {
        try {
            contentResolver.releasePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Berechtigung war bereits weg, z. B. vom Nutzer in den Systemeinstellungen entzogen.
        }
    }

    private suspend fun folderName(treeUri: Uri): String = withContext(Dispatchers.IO) {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        val name = try {
            contentResolver.query(
                documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        } catch (e: Exception) {
            null
        }
        name ?: treeUri.lastPathSegment?.substringAfterLast(':')?.ifBlank { null } ?: "Library"
    }

    private companion object {
        const val TAG = "LibraryRepository"
    }
}
