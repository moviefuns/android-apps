package com.brbrs.bookmarks.data.repository

import com.brbrs.bookmarks.auth.AuthManager
import com.brbrs.bookmarks.data.local.BookmarkDao
import com.brbrs.bookmarks.data.local.BookmarkEntity
import com.brbrs.bookmarks.data.local.FolderDao
import com.brbrs.bookmarks.network.BookmarksApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val folderDao: FolderDao,
    private val apiClient: BookmarksApiClient,
    private val authManager: AuthManager,
) {
    // ── Observe (local) ──────────────────────────────────────────────────────

    fun observeAll(): Flow<List<BookmarkEntity>> = bookmarkDao.observeAll()

    fun search(query: String): Flow<List<BookmarkEntity>> = bookmarkDao.search(query)

    fun filterByTag(tag: String): Flow<List<BookmarkEntity>> = bookmarkDao.filterByTag(tag)

    suspend fun getAllTags(): List<String> {
        val raw = bookmarkDao.getAllTagStrings()
        return raw.flatMap { it.split(",") }.map { it.trim() }
            .filter { it.isNotBlank() }.distinct().sorted()
    }

    suspend fun getById(id: Long): BookmarkEntity? = bookmarkDao.getById(id)

    // ── Write (local + dirty flag) ───────────────────────────────────────────

    suspend fun saveLocal(entity: BookmarkEntity) {
        bookmarkDao.upsert(entity.copy(isDirty = true))
    }

    suspend fun markForDelete(id: Long) {
        val existing = bookmarkDao.getById(id) ?: return
        bookmarkDao.upsert(existing.copy(isPendingDelete = true))
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    suspend fun sync(): Result<Unit> = runCatching {
        val creds = authManager.credentials.firstOrNull() ?: return@runCatching

        // 1. Push dirty
        for (dirty in bookmarkDao.getDirty()) {
            if (dirty.id < 0) {
                // New local bookmark — create on server
                val created = apiClient.createBookmark(creds, dirty)
                bookmarkDao.deleteById(dirty.id)
                bookmarkDao.upsert(created.copy(isDirty = false))
            } else {
                apiClient.updateBookmark(creds, dirty)
                bookmarkDao.upsert(dirty.copy(isDirty = false))
            }
        }

        // 2. Push pending deletes
        for (pending in bookmarkDao.getPendingDelete()) {
            if (pending.id > 0) apiClient.deleteBookmark(creds, pending.id)
            bookmarkDao.deleteById(pending.id)
        }

        // 3. Pull remote
        val remote = apiClient.fetchAllBookmarks(creds)
        bookmarkDao.upsertAll(remote.map { it.copy(isDirty = false) })

        // 4. Pull folders
        val folders = apiClient.fetchFolders(creds)
        folderDao.upsertAll(folders)
    }

    suspend fun clearLocalData() {
        bookmarkDao.deleteAll()
        folderDao.deleteAll()
    }
}
