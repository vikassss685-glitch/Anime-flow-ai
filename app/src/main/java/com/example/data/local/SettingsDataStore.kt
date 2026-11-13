package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "vikas_settings")

enum class AppLanguage(val code: String, val displayLabel: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी")
}

enum class VoiceGender(val label: String, val defaultPitch: Float) {
    FEMALE("Female", 1.12f),
    MALE("Male", 0.84f)
}

data class UserSettings(
    val isDarkTheme: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val voiceGender: VoiceGender = VoiceGender.FEMALE,
    val speechRate: Float = 1.0f,
    val preferOpenModels: Boolean = true
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val VOICE_GENDER = stringPreferencesKey("voice_gender")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val PREFER_OPEN_MODELS = booleanPreferencesKey("prefer_open_models")
    }

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            appLanguage = if (prefs[Keys.APP_LANGUAGE] == AppLanguage.HINDI.code) {
                AppLanguage.HINDI
            } else {
                AppLanguage.ENGLISH
            },
            voiceGender = if (prefs[Keys.VOICE_GENDER] == VoiceGender.MALE.name) {
                VoiceGender.MALE
            } else {
                VoiceGender.FEMALE
            },
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.0f,
            preferOpenModels = prefs[Keys.PREFER_OPEN_MODELS] ?: true
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.settingsDataStore.edit { it[Keys.IS_DARK_THEME] = isDark }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.APP_LANGUAGE] = language.code }
    }

    suspend fun setVoiceGender(gender: VoiceGender) {
        context.settingsDataStore.edit { it[Keys.VOICE_GENDER] = gender.name }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.settingsDataStore.edit { it[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.6f) }
    }

    suspend fun setPreferOpenModels(preferOpen: Boolean) {
        context.settingsDataStore.edit { it[Keys.PREFER_OPEN_MODELS] = preferOpen }
    }
}
