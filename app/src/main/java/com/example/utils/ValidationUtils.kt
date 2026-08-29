package com.example.utils

import android.util.Patterns

object ValidationUtils {

    /**
     * Normalizes a mobile number string into a canonical 10-digit Indian mobile format.
     * Examples:
     * "+91 9876543210" -> "9876543210"
     * "09876543210"    -> "9876543210"
     * "91-98765-43210" -> "9876543210"
     */
    fun normalizeMobile(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var cleaned = input.replace("[^0-9]".toRegex(), "")
        if (cleaned.length == 12 && cleaned.startsWith("91")) {
            cleaned = cleaned.substring(2)
        } else if (cleaned.length == 11 && cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1)
        }
        return cleaned
    }

    /**
     * Validates if a normalized mobile number is a valid 10-digit Indian mobile number.
     * Indian mobile numbers start with 6, 7, 8, or 9.
     */
    fun isValidMobile(mobile: String): Boolean {
        val canonical = normalizeMobile(mobile)
        return canonical.matches("^[6-9]\\d{9}$".toRegex())
    }

    /**
     * Normalizes email: trims and converts to lowercase. Returns null if blank.
     */
    fun normalizeEmail(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim().lowercase()
        return if (trimmed.isBlank()) null else trimmed
    }

    /**
     * Validates email format if non-blank.
     */
    fun isValidEmail(email: String?): Boolean {
        val normalized = normalizeEmail(email) ?: return true // Optional email is valid when empty
        return Patterns.EMAIL_ADDRESS.matcher(normalized).matches()
    }

    /**
     * Validates name: non-blank and reasonable human name length (2 to 50 characters).
     */
    fun isValidName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val trimmed = name.trim()
        return trimmed.length in 2..50
    }
}
