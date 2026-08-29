package com.example.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.i18n.Language
import java.util.Locale

enum class SpeechSessionState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    PROCESSING,
    RESULT,
    ERROR
}

object SpeechRecognitionService {

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeSessionId: Long = 0L

    fun getLocaleForLanguage(language: Language): String {
        return when (language) {
            Language.ENGLISH -> "en-IN"
            Language.HINDI -> "hi-IN"
            Language.MARATHI -> "mr-IN"
            Language.TELUGU -> "te-IN"
            Language.KANNADA -> "kn-IN"
        }
    }

    /**
     * Deduplicates recognized transcripts to prevent repeated word bugs ("hello hello").
     */
    fun deduplicateTranscript(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val tokens = raw.trim().split("\\s+".toRegex())
        val result = mutableListOf<String>()

        for (token in tokens) {
            if (result.isEmpty() || !result.last().equals(token, ignoreCase = true)) {
                result.add(token)
            }
        }
        return result.joinToString(" ")
    }

    fun startListening(
        context: Context,
        language: Language,
        onStateChange: (SpeechSessionState) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopListening() // Clean up any previous recognizer instance

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStateChange(SpeechSessionState.ERROR)
            onError("Speech recognition is not available on this device.")
            return
        }

        val sessionId = System.currentTimeMillis()
        activeSessionId = sessionId

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val localeCode = getLocaleForLanguage(language)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (activeSessionId == sessionId) {
                    onStateChange(SpeechSessionState.LISTENING)
                }
            }

            override fun onBeginningOfSpeech() {
                if (activeSessionId == sessionId) {
                    onStateChange(SpeechSessionState.PROCESSING)
                }
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                if (activeSessionId == sessionId) {
                    onStateChange(SpeechSessionState.PROCESSING)
                }
            }

            override fun onError(error: Int) {
                if (activeSessionId == sessionId) {
                    onStateChange(SpeechSessionState.ERROR)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_CLIENT -> "Client speech error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected."
                        else -> "Speech recognition error ($error)."
                    }
                    onError(msg)
                }
            }

            override fun onResults(results: Bundle?) {
                if (activeSessionId == sessionId) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val rawTranscript = matches?.firstOrNull() ?: ""
                    val cleanText = deduplicateTranscript(rawTranscript)

                    onStateChange(SpeechSessionState.RESULT)
                    onResult(cleanText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onStateChange(SpeechSessionState.LISTENING)
        recognizer.startListening(intent)
    }

    fun stopListening() {
        activeSessionId = 0L
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        } finally {
            speechRecognizer = null
        }
    }
}
