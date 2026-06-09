package com.brbrs.nota.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.nota.R
import com.brbrs.nota.data.NoteEntity
import com.brbrs.nota.ui.components.AddToTasksButton
import com.brbrs.nota.ui.components.NoteImageStrip
import com.brbrs.nota.ui.theme.*
import com.brbrs.nota.ui.viewmodels.NotesListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (Long) -> Unit,
    onNewNote: (String) -> Unit,
    onSettings: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val imageLoader by viewModel.imageLoader.collectAsState()
    val isDark by viewModel.isDark.collectAsState()
    val tasksEnabled by viewModel.tasksEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Brush.verticalGradient(listOf(NavyDeep, NavyMid))
                else Brush.verticalGradient(listOf(LightBg, LightSurface2))
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.nota_logo),
                        contentDescription = "Nóta",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.height(40.dp),
                    )
                    Text(
                        "${uiState.notes.size} notes${if (uiState.isSyncing) " · syncing…" else " · synced"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            color = CyanPrimary,
                            modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                            strokeWidth = 2.dp,
                        )
                    }
                    IconButton(onClick = viewModel::sync) {
                        Icon(Icons.Outlined.Sync, "Sync", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = viewModel::toggleTheme) {
                        Icon(
                            if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Search ───────────────────────────────────────────────────────
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchChanged,
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search notes…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { viewModel.onSearchChanged("") }) {
                        Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }}
                } else null,
                colors = SearchBarDefaults.colors(
                    containerColor = GlassWhite,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                    ),
                ),
                tonalElevation = 0.dp,
            ) {}

            Spacer(Modifier.height(12.dp))

            // ── Category chips ───────────────────────────────────────────────
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        CategoryChip(
                            label = "All",
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.onCategorySelected(null) },
                        )
                    }
                    items(uiState.categories) { cat ->
                        CategoryChip(
                            label = cat,
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.onCategorySelected(cat) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Notes list ───────────────────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                val pinned = uiState.notes.filter { it.favorite }
                val regular = uiState.notes.filter { !it.favorite }

                if (pinned.isNotEmpty()) {
                    item {
                        SectionLabel("Pinned")
                    }
                    items(pinned, key = { it.id }) { note ->
                        NoteCard(note = note, isPrimary = true, imageLoader = imageLoader, tasksEnabled = tasksEnabled, onClick = { onNoteClick(note.id) })
                    }
                    item { SectionLabel("Recent") }
                }

                items(regular, key = { it.id }) { note ->
                    NoteCard(note = note, isPrimary = false, imageLoader = imageLoader, tasksEnabled = tasksEnabled, onClick = { onNoteClick(note.id) })
                }

                item { Spacer(Modifier.height(80.dp)) } // FAB clearance
            }
        }

        // ── FAB ──────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { onNewNote(uiState.selectedCategory ?: "") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 24.dp),
            containerColor = CyanPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Outlined.Add, "New note", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = CyanPrimary.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
              else MaterialTheme.colorScheme.outline
    val textColor = if (selected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
private fun NoteCard(note: NoteEntity, isPrimary: Boolean, imageLoader: coil.ImageLoader?, tasksEnabled: Boolean, onClick: () -> Unit) {
    val isDark = LocalIsDark.current
    val cardTint = if (isDark) Color.White else Color.Black
    val modifier = if (isPrimary)
        Modifier.fillMaxWidth().glassCardPrimary(tint = if (isDark) CyanPrimary else CyanLight)
    else
        Modifier.fillMaxWidth().glassCard(tint = cardTint)

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Column {
            // Title always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    note.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (note.favorite) {
                    Icon(Icons.Filled.PushPin, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp).padding(start = 4.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            if (note.isLocked) {
                // Blurred content placeholder for locked notes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            "Protected note",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // Strip title repetition, headings and image markdown from preview
                val previewText = note.content
                    .replace(Regex("#+\\s*"), "")
                    .replace(Regex("""!\[[^\]]*\]\([^)]+\)"""), "📎 image")
                    .lines()
                    .dropWhile { it.trim() == note.title.trim() || it.isBlank() }
                    .joinToString(" ")
                    .trim()
                    .take(120)
                if (previewText.isNotBlank()) {
                    Text(
                        previewText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Show images from the note content
                NoteImageStrip(
                    markdown = note.content,
                    imageLoader = imageLoader,
                    maxImages = 1,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatTimestamp(note.modified),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AddToTasksButton(
                        noteTitle    = note.title,
                        noteContent  = note.content,
                        tasksEnabled = tasksEnabled,
                        iconOnly     = true,
                    )
                    if (note.category.isNotBlank()) {
                        CategoryBadge(note.category)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

private fun formatTimestamp(epochSeconds: Long): String {
    val date = Date(epochSeconds * 1000)
    val datePart = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(date)
    val timePart = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    return "$datePart | $timePart"
}
