package com.brbrs.bookmarks.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brbrs.bookmarks.data.local.BookmarkEntity
import com.brbrs.bookmarks.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val tags: List<String>              = emptyList(),
    val selectedTag: String?            = null,
    val searchQuery: String             = "",
    val isSyncing: Boolean              = false,
    val syncError: String?              = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookmarkListViewModel @Inject constructor(
    private val repo: BookmarkRepository,
) : ViewModel() {

    private val _searchQuery  = MutableStateFlow("")
    private val _selectedTag  = MutableStateFlow<String?>(null)
    private val _isSyncing    = MutableStateFlow(false)
    private val _syncError    = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ListUiState> = combine(
        _searchQuery,
        _selectedTag,
        _isSyncing,
        _syncError,
    ) { query, tag, syncing, error ->
        Quad(query, tag, syncing, error)
    }.flatMapLatest { (query, tag, syncing, error) ->
        val bookmarkFlow = when {
            query.isNotBlank() -> repo.search(query)
            tag != null        -> repo.filterByTag(tag)
            else               -> repo.observeAll()
        }
        bookmarkFlow.map { bookmarks ->
            ListUiState(
                bookmarks    = bookmarks,
                tags         = emptyList(), // refreshed separately
                selectedTag  = tag,
                searchQuery  = query,
                isSyncing    = syncing,
                syncError    = error,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags

    init {
        sync()
        viewModelScope.launch {
            _tags.value = repo.getAllTags()
        }
    }

    fun onSearchChanged(q: String) { _searchQuery.value = q }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun onDeleteBookmark(id: Long) {
        viewModelScope.launch { repo.markForDelete(id) }
    }

    fun sync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            repo.sync().onFailure { _syncError.value = it.message }
            _isSyncing.value = false
            _tags.value = repo.getAllTags()
        }
    }

    fun dismissSyncError() { _syncError.value = null }
}

private data class Quad<A,B,C,D>(val a: A, val b: B, val c: C, val d: D)
private operator fun <A,B,C,D> Quad<A,B,C,D>.component1() = a
private operator fun <A,B,C,D> Quad<A,B,C,D>.component2() = b
private operator fun <A,B,C,D> Quad<A,B,C,D>.component3() = c
private operator fun <A,B,C,D> Quad<A,B,C,D>.component4() = d
