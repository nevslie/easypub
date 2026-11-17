package io.github.nevslie.easypub.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ein als Library registrierter Ordner. Gespeichert wird nur der Verweis auf den Ordner,
 * die Dateien selbst bleiben unangetastet.
 */
@Entity(tableName = "libraries")
data class LibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** URI aus dem Ordner-Explorer (`ACTION_OPEN_DOCUMENT_TREE`). */
    val treeUri: String,
    val addedAt: Long,
)

/**
 * Ein Buch im Index einer Library. Die Datei ist die Quelle der Wahrheit, diese Tabelle
 * ist nur ein Cache und kann jederzeit durch einen neuen Scan aufgebaut werden.
 *
 * Ein Buch wird innerhalb seiner Library über den [relativePath] identifiziert. So bleibt der
 * Lesefortschritt erhalten, wenn der Ordner neu ausgewählt wird und sich die URIs ändern.
 */
@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = LibraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["libraryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("libraryId"),
        Index(value = ["libraryId", "relativePath"], unique = true),
    ],
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val libraryId: Long,
    val documentUri: String,
    /** Pfad relativ zum Library-Ordner, z. B. `Fantasy/Der Hobbit.epub`. */
    val relativePath: String,
    val title: String,
    val author: String?,
    /** `dc:identifier` aus dem EPUB, falls vorhanden. */
    val identifier: String?,
    /** Absoluter Pfad zum extrahierten Cover im App-Speicher. */
    val coverPath: String?,
    val fileSize: Long,
    val lastModified: Long,
    /** Aktuelle Lesestelle als Readium-Locator im JSON-Format. */
    val locatorJson: String? = null,
    /** Fortschritt im ganzen Buch von 0.0 bis 1.0, basierend auf der aktuellen Lesestelle. */
    val progression: Double? = null,
    val lastOpenedAt: Long? = null,
    val addedAt: Long,
)

data class LibraryWithCount(
    @Embedded val library: LibraryEntity,
    @ColumnInfo(name = "bookCount") val bookCount: Int,
)
