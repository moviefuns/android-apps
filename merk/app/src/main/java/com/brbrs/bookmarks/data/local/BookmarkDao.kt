package com.brbrs.bookmarks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE isPendingDelete = 0 ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("""
        SELECT * FROM bookmarks
        WHERE isPendingDelete = 0
          AND (title LIKE '%' || :q || '%'
               OR url LIKE '%' || :q || '%'
               OR description LIKE '%' || :q || '%'
               OR tags LIKE '%' || :q || '%')
        ORDER BY addedAt DESC
    """)
    fun search(q: String): Flow<List<BookmarkEntity>>

    @Query("""
        SELECT * FROM bookmarks
        WHERE isPendingDelete = 0
          AND (',' || tags || ',' LIKE '%,' || :tag || ',%')
        ORDER BY addedAt DESC
    """)
    fun filterByTag(tag: String): Flow<List<BookmarkEntity>>

    @Query("""
        SELECT * FROM bookmarks
        WHERE isPendingDelete = 0
          AND folderId = :folderId
        ORDER BY addedAt DESC
    """)
    fun filterByFolder(folderId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getById(id: Long): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE isDirty = 1 AND isPendingDelete = 0")
    suspend fun getDirty(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE isPendingDelete = 1")
    suspend fun getPendingDelete(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    // Collect all distinct tags from all non-deleted bookmarks
    @Query("SELECT tags FROM bookmarks WHERE isPendingDelete = 0 AND tags != ''")
    suspend fun getAllTagStrings(): List<String>
}
