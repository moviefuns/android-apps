package com.brbrs.bookmarks

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.brbrs.bookmarks.ui.BookmarksNavHost
import com.brbrs.bookmarks.ui.theme.BookmarksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl   = extractSharedUrl(intent)
        val sharedTitle = extractSharedTitle(intent)

        setContent {
            BookmarksTheme {
                BookmarksNavHost(
                    sharedUrl   = sharedUrl,
                    sharedTitle = sharedTitle,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        // Extract first URL-like token from the shared text
        return text.split("\\s+".toRegex()).firstOrNull {
            it.startsWith("http://") || it.startsWith("https://")
        } ?: text.trim().takeIf { it.startsWith("http") }
    }

    private fun extractSharedTitle(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        return intent.getStringExtra(Intent.EXTRA_SUBJECT)
    }
}
