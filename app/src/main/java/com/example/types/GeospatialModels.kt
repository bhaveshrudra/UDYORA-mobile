package com.example.types

import kotlinx.serialization.Serializable

enum class CatchmentRadius(val label: String, val radiusMeters: Double) {
    RADIUS_5KM("5 KM Catchment", 5000.0),
    RADIUS_10KM("10 KM Catchment", 10000.0),
    BOTH("5 KM & 10 KM Both", 10000.0)
}

@Serializable
data class NearbyResource(
    val id: String,
    val name: String,
    val category: String, // e.g. Dairy Cooperative, Veterinary, Market, Transport, Input Supplier
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val source: String = "OpenStreetMap / Government Registry",
    val sourceType: SourceType = SourceType.GOVERNMENT,
    val verificationStatus: String = "VERIFIED",
    val businessRelevance: String = "HIGH"
)

@Serializable
data class RecommendedBusinessLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val opportunityScore: Int, // 0 to 100
    val reasons: List<String> = emptyList(),
    val businessType: BusinessTypeEnum,
    val source: String = "UDYORA Geospatial Intelligence Engine",
    val confidencePercent: Int = 85
)

@Serializable
data class LocationFactorScore(
    val factorName: String,
    val score: Int, // 0 to 100
    val status: String,
    val description: String
)

@Serializable
data class LocationSuitabilityResult(
    val locationSuitabilityScore: Int, // 0 to 100
    val status: String, // EXCELLENT, SUITABLE, MODERATE, WEAK, REMOTE
    val isRemoteLocation: Boolean,
    val factorScores: List<LocationFactorScore> = emptyList(),
    val nearbyResourceCount5km: Int = 0,
    val nearbyResourceCount10km: Int = 0,
    val confidencePercent: Int = 85
)
