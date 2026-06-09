package com.brbrs.bookmarks.ui.screens.login

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brbrs.bookmarks.ui.theme.CyanPrimary
import com.brbrs.bookmarks.ui.theme.GlassBorder
import com.brbrs.bookmarks.ui.theme.SlateText
import com.brbrs.bookmarks.ui.theme.SlateTextDim

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: LoginViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.loginUrl) {
        state.loginUrl?.let { url ->
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Merk",
                style = MaterialTheme.typography.displaySmall,
                color = CyanPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "for Nextcloud",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextDim,
            )
            Spacer(Modifier.height(48.dp))

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = vm::onServerUrlChanged,
                label = { Text("Nextcloud URL") },
                placeholder = { Text("https://cloud.yourdomain.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { vm.startLoginFlow() }),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = CyanPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor     = SlateText,
                    unfocusedTextColor   = SlateText,
                    cursorColor          = CyanPrimary,
                    focusedLabelColor    = CyanPrimary,
                    unfocusedLabelColor  = SlateTextDim,
                ),
            )

            Spacer(Modifier.height(16.dp))

            if (state.isPolling) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Waiting for browser approval…",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextDim,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Button(
                    onClick   = vm::startLoginFlow,
                    enabled   = state.serverUrl.isNotBlank() && !state.isLoading,
                    modifier  = Modifier.fillMaxWidth().height(48.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape     = MaterialTheme.shapes.medium,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color    = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Connect to Nextcloud", fontSize = 15.sp)
                    }
                }
            }

            state.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
