package com.brbrs.bookmarks.ui.screens.list

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.bookmarks.data.local.BookmarkEntity
import com.brbrs.bookmarks.ui.components.FaviconImage
import com.brbrs.bookmarks.ui.theme.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit,
    vm: BookmarkListViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val tags  by vm.tags.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 56.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Merk",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color      = SlateText,
                        ),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color       = CyanPrimary,
                            )
                        } else {
                            IconButton(onClick = vm::sync) {
                                Icon(Icons.Default.Sync, null, tint = CyanPrimary)
                            }
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, null, tint = SlateTextDim)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = state.searchQuery,
                    onValueChange = vm::onSearchChanged,
                    placeholder   = { Text("Search bookmarks…", color = SlateTextMuted) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = SlateTextMuted) },
                    trailingIcon  = {
                        if (state.searchQuery.isNotBlank())
                            IconButton(onClick = { vm.onSearchChanged("") }) {
                                Icon(Icons.Default.Clear, null, tint = SlateTextMuted)
                            }
                    },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = GlassBorderCyan,
                        unfocusedBorderColor    = GlassBorder,
                        focusedTextColor        = SlateText,
                        unfocusedTextColor      = SlateText,
                        cursorColor             = CyanPrimary,
                        unfocusedContainerColor = Color(0x0DFFFFFF),
                        focusedContainerColor   = Color(0x0DFFFFFF),
                    ),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAdd,
                containerColor = CyanDim,
                contentColor   = CyanPrimary,
                shape          = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, "Add bookmark")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            AnimatedVisibility(visible = tags.isNotEmpty()) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        TagChip(label = "All", selected = state.selectedTag == null) {
                            vm.onTagSelected(null)
                        }
                    }
                    items(tags) { tag ->
                        TagChip(label = tag, selected = state.selectedTag == tag) {
                            vm.onTagSelected(tag)
                        }
                    }
                }
            }

            state.syncError?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color    = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape    = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            err,
                            modifier = Modifier.weight(1f),
                            color    = MaterialTheme.colorScheme.error,
                            style    = MaterialTheme.typography.bodySmall,
                        )
                        IconButton(onClick = vm::dismissSyncError, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (state.bookmarks.isEmpty() && !state.isSyncing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text  = if (state.searchQuery.isNotBlank()) "No results" else "No bookmarks yet",
                        color = SlateTextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier            = Modifier.fillMaxSize(),
                ) {
                    items(state.bookmarks, key = { it.id }) { bookmark ->
                        BookmarkCard(
                            modifier    = Modifier.animateItemPlacement(),
                            bookmark    = bookmark,
                            onClick     = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.url))
                                )
                            },
                            onEdit   = { onEdit(bookmark.id) },
                            onDelete = { vm.onDeleteBookmark(bookmark.id) },
                        )
                    }
                }
            }
        }
    }
}

// ── Tag chip ──────────────────────────────────────────────────────────────────

@Composable
private fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        color    = if (selected) CyanDim else Color(0x0AFFFFFF),
        modifier = Modifier.height(30.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.padding(horizontal = 12.dp),
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) CyanPrimary else SlateTextDim,
            )
        }
    }
}

// ── Bookmark card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkCard(
    bookmark: BookmarkEntity,
    onClick:  () -> Unit,
    onEdit:   () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier       = modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(14.dp),
        color          = Color(0x0AFFFFFF),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick     = onClick,
                    onLongClick = { showMenu = true },
                )
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FaviconImage(url = bookmark.faviconUrl, title = bookmark.title)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = bookmark.title.ifBlank { bookmark.url },
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color    = SlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = Uri.parse(bookmark.url).host ?: bookmark.url,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = SlateTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (bookmark.tags.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        bookmark.tags.split(",").take(3).filter { it.isNotBlank() }.forEach { tag ->
                            Text(
                                text     = tag.trim(),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x1463B3ED))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color    = CyanPrimary,
                            )
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = SlateTextMuted, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded        = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text    = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                    )
                    DropdownMenuItem(
                        text    = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    )
                }
            }
        }
    }
}
