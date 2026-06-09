package com.brbrs.bookmarks.ui.screens.applock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.brbrs.bookmarks.biometric.BiometricHelper
import com.brbrs.bookmarks.ui.theme.CyanPrimary
import com.brbrs.bookmarks.ui.theme.SlateTextDim

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        BiometricHelper.prompt(
            activity  = activity,
            onSuccess = onUnlocked,
            onFail    = { msg -> error = msg },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier              = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally,
        ) {
            Text("Bookmarks", style = MaterialTheme.typography.displaySmall, color = CyanPrimary)
            Spacer(Modifier.height(32.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
            }
            OutlinedButton(onClick = {
                error = null
                BiometricHelper.prompt(activity, onSuccess = onUnlocked, onFail = { msg -> error = msg })
            }) {
                Text("Unlock", color = CyanPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text("Use biometrics or device PIN", color = SlateTextDim,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
