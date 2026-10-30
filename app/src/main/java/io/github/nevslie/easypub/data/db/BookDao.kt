package io.github.nevslie.easypub.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE libraryId = :libraryId ORDER BY title COLLATE NOCASE")
    fun observeByLibrary(libraryId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE libraryId = :libraryId")
    suspend fun getByLibrary(libraryId: Long): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun get(id: Long): BookEntity?

    @Insert
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Query("DELETE FROM books WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query(
        """
        UPDATE books
        SET locatorJson = :locatorJson, progression = :progression, lastOpenedAt = :openedAt
        WHERE id = :id
        """
    )
    suspend fun updateProgress(id: Long, locatorJson: String, progression: Double?, openedAt: Long)
}
