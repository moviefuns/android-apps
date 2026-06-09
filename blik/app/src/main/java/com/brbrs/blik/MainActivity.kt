package com.brbrs.blik

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import com.brbrs.blik.network.WebDavClient
import com.brbrs.blik.ui.BlikNavHost
import com.brbrs.blik.ui.theme.BlikTheme
import com.brbrs.blik.ui.theme.ThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import coil.ImageLoader

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var themeRepository: ThemeRepository
    @Inject lateinit var webDavClient: WebDavClient
    @Inject lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by themeRepository.isDark.collectAsStateWithLifecycle(initialValue = true)
            BlikTheme(isDark = isDark) {
                BlikNavHost(webDavClient = webDavClient, imageLoader = imageLoader)
            }
        }
    }
}
