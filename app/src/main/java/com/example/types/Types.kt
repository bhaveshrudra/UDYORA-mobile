package com.example.types

import kotlinx.serialization.Serializable

@Serializable
data class BusinessAssessment(
    val id: String,
    val userId: String,
    val date: Long,
    val location: LocationData,
    val businessType: String,
    val description: String,
    val availableCapital: Double,
    val feasibilityScore: Int,
    val dataConfidence: String,
    val recommendations: List<String>
)

@Serializable
data class LocationData(
    val state: String,
    val district: String,
    val mandal: String,
    val pincode: String,
    val lat: Double,
    val lng: Double
)
