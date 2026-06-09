package com.brbrs.merk.ui.screens.list

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.merk.data.local.BookmarkEntity
import com.brbrs.merk.ui.components.FaviconImage
import com.brbrs.merk.ui.components.MerkLogo
import com.brbrs.merk.ui.components.RemindMeButton
import com.brbrs.merk.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpen: (Long) -> Unit,
    onSettings: () -> Unit,
    vm: BookmarkListViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val tags  by vm.tags.collectAsState()
    val isDark = state.isDark

    val bgBrush   = if (isDark) Brush.verticalGradient(listOf(NavyDeep, NavyMid))
                    else Brush.verticalGradient(listOf(LightBg, LightSurface2))
    val cardTint  = if (isDark) Color.White else Color.Black
    val iconTint  = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = MaterialTheme.colorScheme.onBackground
    val subColor   = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    MerkLogo(modifier = Modifier.height(44.dp))
                    Text(
                        "${state.bookmarks.size} bookmarks${if (state.isSyncing) " · syncing..." else " · synced"}",
                        style = MaterialTheme.typography.bodyMedium, color = subColor,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = vm::sync) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Sync, "Sync", tint = iconTint)
                        }
                    }
                    IconButton(onClick = vm::toggleTheme) {
                        Icon(
                            if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDark) "Switch to light" else "Switch to dark",
                            tint = iconTint,
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, "Settings", tint = iconTint)
                    }
                }
            }

            // ── Search ────────────────────────────────────────────────────────
            SearchBar(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                query = state.searchQuery, onQueryChange = vm::onSearchChanged,
                onSearch = {}, active = false, onActiveChange = {},
                placeholder = { Text("Search bookmarks…", color = subColor) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = iconTint) },
                trailingIcon = if (state.searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { vm.onSearchChanged("") }) {
                        Icon(Icons.Outlined.Close, null, tint = iconTint) } }
                } else null,
                colors = SearchBarDefaults.colors(
                    containerColor = if (isDark) GlassWhite else LightSurface,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = titleColor, unfocusedTextColor = titleColor,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                ),
                tonalElevation = 0.dp,
            ) {}

            Spacer(Modifier.height(12.dp))

            // ── Tag chips ─────────────────────────────────────────────────────
            if (tags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item { TagChip("All", state.selectedTag == null, isDark) { vm.onTagSelected(null) } }
                    items(tags) { tag ->
                        TagChip(tag, state.selectedTag == tag, isDark) { vm.onTagSelected(tag) }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Sync error ────────────────────────────────────────────────────
            state.syncError?.let { err ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(err, modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = vm::dismissSyncError, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ── List ──────────────────────────────────────────────────────────
            if (state.bookmarks.isEmpty() && !state.isSyncing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (state.searchQuery.isNotBlank()) "No results" else "No bookmarks yet",
                        color = subColor, style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.bookmarks, key = { it.id }) { bookmark ->
                        BookmarkCard(
                            bookmark     = bookmark,
                            tasksEnabled = state.tasksEnabled,
                            cardTint     = cardTint,
                            titleColor   = titleColor,
                            subColor     = subColor,
                            isDark       = isDark,
                            onClick      = { onOpen(bookmark.id) },
                            onEdit       = { onEdit(bookmark.id) },
                            onDelete     = { vm.onDeleteBookmark(bookmark.id) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = onAdd,
            modifier       = Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
                .padding(end = 24.dp, bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = if (isDark) NavyDeep else Color.White,
            shape          = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Outlined.Add, "Add bookmark", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun TagChip(label: String, selected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) primary.copy(alpha = 0.15f)
                        else if (isDark) GlassWhite else LightSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkCard(
    bookmark: BookmarkEntity,
    tasksEnabled: Boolean,
    cardTint: Color,
    titleColor: Color,
    subColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val tagList  = remember(bookmark.tags) {
        bookmark.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(tint = cardTint)
            .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FaviconImage(url = bookmark.faviconUrl, title = bookmark.title)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bookmark.title.ifBlank { bookmark.url },
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(Uri.parse(bookmark.url).host ?: bookmark.url,
                            style = MaterialTheme.typography.bodyMedium,
                            color = subColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RemindMeButton(bookmarkTitle = bookmark.title, bookmarkUrl = bookmark.url,
                        tasksEnabled = tasksEnabled, iconOnly = true)
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.MoreVert, null, tint = subColor,
                                modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") },
                                onClick = { showMenu = false; onEdit() },
                                leadingIcon = { Icon(Icons.Outlined.Edit, null) })
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null,
                                    tint = MaterialTheme.colorScheme.error) })
                        }
                    }
                }
            }

            if (tagList.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tagList.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(tag, style = MaterialTheme.typography.labelSmall,
                                color = primary.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}
