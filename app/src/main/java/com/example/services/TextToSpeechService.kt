package com.example.services

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.i18n.Language
import java.util.Locale

object TextToSpeechService {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun init(context: Context, onReady: (Boolean) -> Unit) {
        if (tts != null && isInitialized) {
            onReady(true)
            return
        }

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onReady(true)
            } else {
                isInitialized = false
                onReady(false)
            }
        }
    }

    fun speak(text: String, language: Language, onVoiceUnavailable: () -> Unit) {
        val ttsInstance = tts
        if (ttsInstance == null || !isInitialized) {
            onVoiceUnavailable()
            return
        }

        val targetLocale = when (language) {
            Language.ENGLISH -> Locale("en", "IN")
            Language.HINDI -> Locale("hi", "IN")
            Language.MARATHI -> Locale("mr", "IN")
            Language.TELUGU -> Locale("te", "IN")
            Language.KANNADA -> Locale("kn", "IN")
        }

        val result = ttsInstance.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            onVoiceUnavailable()
        } else {
            ttsInstance.stop()
            @Suppress("DEPRECATION")
            ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UDYORA_TTS_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        } finally {
            tts = null
            isInitialized = false
        }
    }
}
