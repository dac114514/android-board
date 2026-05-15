package com.faster.aiboard.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("deepseek_api_key")
        val QUICK_QUESTIONS = stringPreferencesKey("quick_questions")
        val TOOL_COLUMNS = stringPreferencesKey("tool_columns")
        val DEFAULT_TEMPLATE = stringPreferencesKey("default_template")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }
}
