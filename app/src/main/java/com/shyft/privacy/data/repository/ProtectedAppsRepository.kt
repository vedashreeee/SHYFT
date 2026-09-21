package com.shyft.privacy.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shyft_protected_apps")

class ProtectedAppsRepository(private val context: Context) {

    private val protectedAppsKey = stringSetPreferencesKey("selected_protected_app_ids")

    private val defaultSelectedAppIds = setOf(
        "phone", "gpay", "whatsapp"
    )

    val protectedAppIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[protectedAppsKey] ?: defaultSelectedAppIds
        }

    suspend fun toggleAppProtection(appId: String, isProtected: Boolean) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[protectedAppsKey] ?: defaultSelectedAppIds
            val updatedSet = if (isProtected) {
                currentSet + appId
            } else {
                currentSet - appId
            }
            preferences[protectedAppsKey] = updatedSet
        }
    }

    suspend fun setProtectedApps(appIds: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[protectedAppsKey] = appIds
        }
    }
}
