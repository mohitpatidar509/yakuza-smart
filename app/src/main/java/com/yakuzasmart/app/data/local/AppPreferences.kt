package com.yakuzasmart.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yakuzasmart.app.core.localization.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "yakuza_settings_prefs")

class AppPreferences(private val context: Context) {
    private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_SIMULATOR_MODE = booleanPreferencesKey("simulator_mode_enabled")
    private val KEY_SYNC_ON_CONNECT = booleanPreferencesKey("sync_on_connect_enabled")
    private val KEY_LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
    private val KEY_LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")

    val languageFlow: Flow<Language> = context.dataStore.data.map { prefs ->
        val code = prefs[KEY_LANGUAGE] ?: Language.ENGLISH.code
        Language.entries.find { it.code == code } ?: Language.ENGLISH
    }

    val isSimulatorEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SIMULATOR_MODE] ?: false
    }

    val isSyncOnConnectEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SYNC_ON_CONNECT] ?: false
    }

    val lastDeviceFlow: Flow<Pair<String?, String?>> = context.dataStore.data.map { prefs ->
        Pair(prefs[KEY_LAST_DEVICE_ADDRESS], prefs[KEY_LAST_DEVICE_NAME])
    }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun setSimulatorEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SIMULATOR_MODE] = enabled
        }
    }

    suspend fun setSyncOnConnectEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SYNC_ON_CONNECT] = enabled
        }
    }

    suspend fun saveLastDevice(address: String, name: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_DEVICE_ADDRESS] = address
            if (name != null) prefs[KEY_LAST_DEVICE_NAME] = name
        }
    }

    suspend fun forgetLastDevice() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_LAST_DEVICE_ADDRESS)
            prefs.remove(KEY_LAST_DEVICE_NAME)
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
