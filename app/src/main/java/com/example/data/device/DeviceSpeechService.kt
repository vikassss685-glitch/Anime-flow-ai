package com.example.data.device

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.local.VoiceGender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class DeviceSpeechService(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsAvailable = MutableStateFlow(false)
    val ttsAvailable: StateFlow<Boolean> = _ttsAvailable.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            isReady = status == TextToSpeech.SUCCESS
            _ttsAvailable.value = isReady
            if (isReady) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(
        text: String,
        languageLabel: String,
        gender: VoiceGender,
        speechRate: Float
    ): Boolean {
        val engine = tts ?: return false
        if (!isReady || text.isBlank()) return false

        val locale = localeForLanguage(languageLabel)
        val langResult = engine.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }
        engine.setPitch(gender.defaultPitch)
        engine.setSpeechRate(speechRate)
        val utteranceId = UUID.randomUUID().toString()
        val result = engine.speak(text.take(3500), TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.SUCCESS) {
            _isSpeaking.value = true
            return true
        }
        return false
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        _isSpeaking.value = false
    }

    companion object {
        fun localeForLanguage(language: String): Locale {
            return when (language.lowercase(Locale.ROOT)) {
                "hindi", "हिन्दी", "hi" -> Locale.forLanguageTag("hi-IN")
                "japanese", "ja" -> Locale.JAPANESE
                "chinese", "zh" -> Locale.SIMPLIFIED_CHINESE
                "korean", "ko" -> Locale.KOREAN
                "arabic", "ar" -> Locale.forLanguageTag("ar-SA")
                "spanish", "es" -> Locale.forLanguageTag("es-ES")
                "french", "fr" -> Locale.FRENCH
                else -> Locale.US
            }
        }
    }
}
