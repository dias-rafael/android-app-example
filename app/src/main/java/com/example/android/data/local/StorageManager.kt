package com.example.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // EncryptedSharedPreferences (Secure Storage)
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSecureData(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun getSecureData(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    // DataStore (Modern Preference Storage)
    private val USER_TOKEN = stringPreferencesKey("user_token")

    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
        }
    }
}