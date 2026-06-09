package com.brbrs.bookmarks.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.bookmarks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SlateTextDim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            state.serverUrl?.let { url ->
                SettingsSection(title = "Account") {
                    SettingsInfoRow(icon = Icons.Default.Cloud, label = "Server", value = url)
                    state.username?.let { user ->
                        SettingsInfoRow(icon = Icons.Default.Person, label = "User", value = user)
                    }
                }
            }

            SettingsSection(title = "Security") {
                SettingsToggleRow(
                    icon     = Icons.Default.Fingerprint,
                    label    = "Biometric app lock",
                    checked  = state.biometricEnabled,
                    enabled  = state.biometricAvailable,
                    onToggle = vm::toggleBiometric,
                )
            }

            SettingsSection(title = "Data") {
                SettingsActionRow(
                    icon    = Icons.Default.Sync,
                    label   = "Sync now",
                    loading = state.isSyncing,
                    onClick = vm::sync,
                )
            }

            Spacer(Modifier.weight(1f))

            var confirmLogout by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick  = { confirmLogout = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Disconnect account")
            }

            Spacer(Modifier.height(24.dp))

            if (confirmLogout) {
                AlertDialog(
                    onDismissRequest = { confirmLogout = false },
                    title = { Text("Disconnect?") },
                    text  = { Text("This will remove all local data.", color = SlateTextDim) },
                    confirmButton = {
                        TextButton(onClick = { vm.logout(); confirmLogout = false }) {
                            Text("Disconnect", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmLogout = false }) { Text("Cancel") }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        style    = MaterialTheme.typography.labelSmall,
        color    = SlateTextMuted,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x0AFFFFFF),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = SlateTextDim, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = SlateText)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = SlateTextDim, modifier = Modifier.size(20.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) SlateText else SlateTextMuted,
            )
        }
        Switch(
            checked         = checked,
            enabled         = enabled,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor = CyanPrimary,
                checkedTrackColor = CyanDim,
            ),
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = SlateTextDim, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = SlateText)
        }
        if (loading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color       = CyanPrimary,
            )
        } else {
            IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, null, tint = SlateTextMuted)
            }
        }
    }
}
