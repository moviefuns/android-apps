package com.brbrs.vinci.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.vinci.ui.theme.*
import com.brbrs.vinci.ui.viewmodels.RestoreViewModel

@Composable
fun RestoreScreen(
    onDone: () -> Unit,
    viewModel: RestoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark  = LocalIsDark.current

    LaunchedEffect(uiState.done) {
        if (uiState.done) onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Brush.verticalGradient(listOf(NavyDeep, NavyMid))
                else Brush.verticalGradient(listOf(LightBg, LightSurface2))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .glassCard(cornerRadius = 24.dp, bgAlpha = 0.08f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (uiState.isRestoring) Icons.Outlined.CloudSync
                                  else Icons.Outlined.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }

            // Title
            Text(
                "Restore from Nextcloud",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            // Body
            AnimatedContent(targetState = uiState.isRestoring, label = "restore_body") { restoring ->
                if (restoring) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(color = CyanPrimary)
                        Text(
                            "Scanning Vinci folder on Nextcloud...\nThis may take a moment.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        "Vinci found your previous data on Nextcloud.\nTap Restore to bring back your starred contacts, follow-ups, interaction logs, and display settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Error
            if (uiState.error != null) {
                Text(
                    "Could not restore: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            // Buttons
            if (!uiState.isRestoring) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::restore,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Outlined.CloudDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Restore my data", style = MaterialTheme.typography.titleMedium)
                    }

                    OutlinedButton(
                        onClick = viewModel::skip,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text("Start fresh", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
