package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: Long,
    val locationJson: String,
    val businessType: String,
    val description: String,
    val availableCapital: Double,
    val feasibilityScore: Int,
    val dataConfidence: String,
    val recommendationsJson: String
)
