package com.brbrs.bookmarks.ui.screens.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.bookmarks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookmarkScreen(
    prefillUrl:   String? = null,
    prefillTitle: String? = null,
    onSaved:   () -> Unit,
    onDismiss: () -> Unit,
    vm: EditBookmarkViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val isEdit = state.id > 0

    // Apply shared URL/title once the VM is ready
    LaunchedEffect(prefillUrl) {
        if (prefillUrl != null) vm.prefill(prefillUrl, prefillTitle)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (prefillUrl != null) "Save to Merk"
                        else if (isEdit) "Edit bookmark"
                        else "Add bookmark"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cancel", tint = SlateTextDim)
                    }
                },
                actions = {
                    TextButton(onClick = vm::save, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CyanPrimary)
                        } else {
                            Text("Save", color = CyanPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Show a shared-from banner when opened via share sheet
            if (prefillUrl != null) {
                Surface(
                    color = CyanDim,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Share, null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                        Text("Shared from browser", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                    }
                }
            }

            BookmarkTextField(value = state.url, onValue = vm::onUrlChanged,
                label = "URL *", placeholder = "https://", keyboardType = KeyboardType.Uri)

            BookmarkTextField(value = state.title, onValue = vm::onTitleChanged,
                label = "Title", placeholder = "Leave blank to use URL")

            BookmarkTextField(value = state.description, onValue = vm::onDescriptionChanged,
                label = "Description", placeholder = "Optional notes",
                singleLine = false, minLines = 2)

            // Tags
            SectionLabel("Tags")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0DFFFFFF))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.tags.forEach { tag ->
                    TagChip(tag = tag, onRemove = { vm.removeTag(tag) })
                }
                OutlinedTextField(
                    value = state.tagInput,
                    onValueChange = vm::onTagInputChanged,
                    placeholder = { Text("Add tag…", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { vm.addTag() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor   = Color.Transparent,
                        focusedTextColor     = SlateText,
                        unfocusedTextColor   = SlateText,
                        cursorColor          = CyanPrimary,
                    ),
                )
            }

            if (state.allTags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.allTags.filterNot { it in state.tags }.take(10)) { tag ->
                        SuggestionChip(onClick = { vm.addTag(tag) }, label = { Text(tag) })
                    }
                }
            }

            // Folder picker
            if (state.folders.isNotEmpty()) {
                SectionLabel("Folder")
                var expanded by remember { mutableStateOf(false) }
                val currentFolder = state.folders.find { it.id == state.folderId }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = currentFolder?.title ?: "No folder",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GlassBorderCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor     = SlateText,
                            unfocusedTextColor   = SlateText,
                        ),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("No folder") },
                            onClick = { vm.onFolderSelected(-1L); expanded = false })
                        state.folders.forEach { folder ->
                            DropdownMenuItem(text = { Text(folder.title) },
                                onClick = { vm.onFolderSelected(folder.id); expanded = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BookmarkTextField(
    value: String, onValue: (String) -> Unit,
    label: String, placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true, minLines: Int = 1,
) {
    OutlinedTextField(
        value = value, onValueChange = onValue,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = SlateTextMuted) },
        singleLine = singleLine, minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GlassBorderCyan,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor     = SlateText,
            unfocusedTextColor   = SlateText,
            cursorColor          = CyanPrimary,
            focusedLabelColor    = CyanPrimary,
            unfocusedLabelColor  = SlateTextDim,
        ),
    )
}

@Composable private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
}

@Composable
private fun TagChip(tag: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x1463B3ED))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(tag, style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
        Icon(Icons.Default.Close, "Remove", tint = CyanPrimary,
            modifier = Modifier.size(12.dp).clickable { onRemove() })
    }
}
