package com.brbrs.vinci.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brbrs.vinci.network.WebDavRepository
import com.brbrs.vinci.ui.theme.DisplayPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestoreUiState(
    val isRestoring: Boolean = false,
    val contactsRestored: Int = 0,
    val logsImported: Int = 0,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val webDavRepository: WebDavRepository,
    private val displayPrefs: DisplayPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreUiState())
    val uiState: StateFlow<RestoreUiState> = _uiState.asStateFlow()

    fun restore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null) }

            // 1. Restore display preferences from Nextcloud
            runCatching {
                val prefsMap = webDavRepository.downloadPreferences()
                if (prefsMap != null) displayPrefs.fromMap(prefsMap)
            }

            // 2. Restore contacts (starred, followUp) and interaction logs
            val result = webDavRepository.restoreFromNextcloud()

            if (result.error != null) {
                _uiState.update { it.copy(isRestoring = false, error = result.error) }
            } else {
                _uiState.update {
                    it.copy(
                        isRestoring      = false,
                        contactsRestored = result.contactsRestored,
                        logsImported     = result.logsImported,
                        done             = true,
                    )
                }
            }
        }
    }

    fun skip() {
        _uiState.update { it.copy(done = true) }
    }
}
