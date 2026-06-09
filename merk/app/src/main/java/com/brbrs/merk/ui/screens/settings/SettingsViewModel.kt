package com.brbrs.merk.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brbrs.merk.auth.AuthManager
import com.brbrs.merk.biometric.BiometricHelper
import com.brbrs.merk.data.repository.BookmarkRepository
import com.brbrs.merk.tasks.TasksOrgHelper
import com.brbrs.merk.tasks.TasksPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String?          = null,
    val username: String?           = null,
    val biometricEnabled: Boolean   = false,
    val biometricAvailable: Boolean = false,
    val tasksEnabled: Boolean       = false,
    val tasksInstalled: Boolean     = false,
    val isSyncing: Boolean          = false,
    val lastSynced: String          = "Never",
    val loggedOut: Boolean          = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager,
    private val repo: BookmarkRepository,
    private val tasksPref: TasksPreference,
) : ViewModel() {

    private val _syncing    = MutableStateFlow(false)
    private val _loggedOut  = MutableStateFlow(false)
    private val _lastSynced = MutableStateFlow("Never")

    val uiState: StateFlow<SettingsUiState> = combine(
        authManager.credentials,
        authManager.biometricEnabled,
        tasksPref.enabled,
        _syncing,
        _loggedOut,
        _lastSynced,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val creds      = arr[0] as? com.brbrs.merk.auth.AuthCredentials
        val bio        = arr[1] as Boolean
        val tasks      = arr[2] as Boolean
        val syncing    = arr[3] as Boolean
        val loggedOut  = arr[4] as Boolean
        val lastSynced = arr[5] as String
        SettingsUiState(
            serverUrl          = creds?.serverUrl,
            username           = creds?.username,
            biometricEnabled   = bio,
            biometricAvailable = BiometricHelper.isAvailable(context),
            tasksEnabled       = tasks,
            tasksInstalled     = TasksOrgHelper.isInstalled(context),
            isSyncing          = syncing,
            lastSynced         = lastSynced,
            loggedOut          = loggedOut,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch { authManager.setBiometricEnabled(enabled) }
    }

    fun toggleTasks(enabled: Boolean) {
        viewModelScope.launch { tasksPref.setEnabled(enabled) }
    }

    fun sync() {
        if (_syncing.value) return
        viewModelScope.launch {
            _syncing.value = true
            repo.sync().onSuccess {
                _lastSynced.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            }
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
