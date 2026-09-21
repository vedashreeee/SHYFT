package com.shyft.privacy.ui.screens.protectedapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shyft.privacy.data.repository.ProtectedAppsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProtectedAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProtectedAppsRepository(application.applicationContext)

    val protectedAppIds: StateFlow<Set<String>> = repository.protectedAppIdsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf("phone", "gpay", "whatsapp")
        )

    fun toggleAppProtection(appId: String, isProtected: Boolean) {
        viewModelScope.launch {
            repository.toggleAppProtection(appId, isProtected)
        }
    }
}
