package com.example.types

import kotlinx.serialization.Serializable

@Serializable
enum class LocationSource {
    MANUAL,
    GPS,
    PINCODE,
    DEMO
}

@Serializable
enum class LocationVerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    INCONSISTENT,
    ERROR
}

enum class GpsState {
    IDLE,
    REQUESTING_PERMISSION,
    LOCATING,
    RESOLVING,
    RESOLVED,
    CONFIRMED,
    DENIED,
    ERROR
}

@Serializable
data class CanonicalLocation(
    val source: LocationSource = LocationSource.MANUAL,
    val stateId: String? = null,
    val stateName: String? = null,
    val districtId: String? = null,
    val districtName: String? = null,
    val mandalId: String? = null,
    val mandalName: String? = null,
    val pincode: String? = null,
    val locality: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val timestamp: Long? = null,
    val verificationStatus: LocationVerificationStatus = LocationVerificationStatus.UNVERIFIED,
    val locationRevision: Long = System.currentTimeMillis()
) {
    val isVerified: Boolean
        get() = verificationStatus == LocationVerificationStatus.VERIFIED

    val isDemo: Boolean
        get() = source == LocationSource.DEMO

    val displayText: String
        get() {
            val parts = listOfNotNull(mandalName, districtName, stateName)
            val main = if (parts.isNotEmpty()) parts.joinToString(", ") else "Unspecified Location"
            return if (!pincode.isNullOrBlank()) "$main - $pincode" else main
        }
}
