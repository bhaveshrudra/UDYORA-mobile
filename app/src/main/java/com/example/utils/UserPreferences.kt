package com.example.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object UserPreferences {
    private val LANGUAGE_KEY = stringPreferencesKey("preferred_language")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val FIRST_LAUNCH_COMPLETED_KEY = booleanPreferencesKey("first_launch_completed")

    fun getPreferredLanguage(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY]
        }
    }

    suspend fun setPreferredLanguage(context: Context, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    fun getUserId(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun setUserId(context: Context, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun isFirstLaunchCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH_COMPLETED_KEY] ?: false
        }
    }

    suspend fun setFirstLaunchCompleted(context: Context, completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_COMPLETED_KEY] = completed
        }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}
