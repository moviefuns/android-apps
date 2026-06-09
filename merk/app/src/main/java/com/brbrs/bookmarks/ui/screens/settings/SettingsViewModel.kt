package com.brbrs.bookmarks.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brbrs.bookmarks.auth.AuthManager
import com.brbrs.bookmarks.biometric.BiometricHelper
import com.brbrs.bookmarks.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String?         = null,
    val username: String?          = null,
    val biometricEnabled: Boolean  = false,
    val biometricAvailable: Boolean = false,
    val isSyncing: Boolean         = false,
    val loggedOut: Boolean         = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager,
    private val repo: BookmarkRepository,
) : ViewModel() {

    private val _syncing   = MutableStateFlow(false)
    private val _loggedOut = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        authManager.credentials,
        authManager.biometricEnabled,
        _syncing,
        _loggedOut,
    ) { creds, bio, syncing, loggedOut ->
        SettingsUiState(
            serverUrl          = creds?.serverUrl,
            username           = creds?.username,
            biometricEnabled   = bio,
            biometricAvailable = BiometricHelper.isAvailable(context),
            isSyncing          = syncing,
            loggedOut          = loggedOut,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch { authManager.setBiometricEnabled(enabled) }
    }

    fun sync() {
        if (_syncing.value) return
        viewModelScope.launch {
            _syncing.value = true
            repo.sync()
            _syncing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.clearLocalData()
            authManager.clearCredentials()
            _loggedOut.value = true
        }
    }
}
