package com.example.services

import com.example.BuildConfig

object GeminiModelConfig {
    const val MODEL_NAME = "gemini-1.5-flash"
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
    const val TIMEOUT_SECONDS = 20L
    const val MAX_RETRIES = 2

    fun getApiKey(): String {
        return try {
            val key = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            if (!key.isNullOrBlank() && key != "DEFAULT_KEY") key else ""
        } catch (e: Exception) {
            ""
        }
    }
}
