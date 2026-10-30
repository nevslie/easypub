package io.github.nevslie.easypub.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query(
        """
        SELECT libraries.*, COUNT(books.id) AS bookCount
        FROM libraries
        LEFT JOIN books ON books.libraryId = libraries.id
        GROUP BY libraries.id
        ORDER BY libraries.name COLLATE NOCASE
        """
    )
    fun observeAllWithCount(): Flow<List<LibraryWithCount>>

    @Query("SELECT * FROM libraries WHERE id = :id")
    fun observe(id: Long): Flow<LibraryEntity?>

    @Query("SELECT * FROM libraries WHERE id = :id")
    suspend fun get(id: Long): LibraryEntity?

    @Query("SELECT * FROM libraries")
    suspend fun getAll(): List<LibraryEntity>

    @Query("SELECT * FROM libraries WHERE treeUri = :treeUri LIMIT 1")
    suspend fun findByTreeUri(treeUri: String): LibraryEntity?

    @Insert
    suspend fun insert(library: LibraryEntity): Long

    @Query("UPDATE libraries SET treeUri = :treeUri, name = :name WHERE id = :id")
    suspend fun updateFolder(id: Long, treeUri: String, name: String)

    @Query("DELETE FROM libraries WHERE id = :id")
    suspend fun delete(id: Long)
}
