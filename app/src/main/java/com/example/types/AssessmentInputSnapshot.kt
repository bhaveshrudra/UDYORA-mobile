package com.example.types

import kotlinx.serialization.Serializable

@Serializable
data class AssessmentInputSnapshot(
    val assessmentRunId: String,
    val userId: String,
    val languageCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val locationSnapshot: CanonicalLocation,
    val businessSnapshot: BusinessSnapshot,
    val financialSnapshot: FinancialOutput
)
