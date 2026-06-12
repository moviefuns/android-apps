package com.brbrs.merk.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brbrs.merk.data.local.BookmarkEntity
import com.brbrs.merk.data.repository.BookmarkRepository
import com.brbrs.merk.tasks.TasksPreference
import com.brbrs.merk.ui.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val selectedTag: String?            = null,
    val searchQuery: String             = "",
    val isSyncing: Boolean              = false,
    val syncError: String?              = null,
    val tasksEnabled: Boolean           = false,
    val isDark: Boolean                 = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookmarkListViewModel @Inject constructor(
    private val repo: BookmarkRepository,
    private val tasksPref: TasksPreference,
    private val themeRepo: ThemeRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _isSyncing   = MutableStateFlow(false)
    private val _syncError   = MutableStateFlow<String?>(null)

    // Combine query+tag into a single flow first, then combine with the rest
    private val _filters = combine(_searchQuery, _selectedTag) { q, t -> q to t }

    val uiState: StateFlow<ListUiState> = combine(
        _filters, _isSyncing, _syncError, tasksPref.enabled, themeRepo.isDark,
    ) { (query, tag), syncing, error, tasks, dark ->
        QueryContext(query, tag, syncing, error, tasks, dark)
    }.flatMapLatest { ctx ->
        val bookmarkFlow = when {
            ctx.query.isNotBlank() -> repo.search(ctx.query)
            ctx.tag != null        -> repo.filterByTag(ctx.tag)
            else                   -> repo.observeAll()
        }
        bookmarkFlow.map { bms ->
            ListUiState(
                bookmarks    = bms,
                selectedTag  = ctx.tag,
                searchQuery  = ctx.query,
                isSyncing    = ctx.syncing,
                syncError    = ctx.error,
                tasksEnabled = ctx.tasks,
                isDark       = ctx.dark,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags

    init {
        sync()
        viewModelScope.launch { _tags.value = repo.getAllTags() }
    }

    fun onSearchChanged(q: String) { _searchQuery.value = q }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun onDeleteBookmark(id: Long) {
        viewModelScope.launch {
            repo.markForDelete(id)
            repo.sync()  // push deletion to server immediately
        }
    }

    fun toggleTheme() {
        viewModelScope.launch { themeRepo.setDark(!uiState.value.isDark) }
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

private data class QueryContext(
    val query: String,
    val tag: String?,
    val syncing: Boolean,
    val error: String?,
    val tasks: Boolean,
    val dark: Boolean,
)
