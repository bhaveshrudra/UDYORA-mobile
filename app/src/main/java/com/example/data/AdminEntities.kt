package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AdminRole {
    CHIEF_ADMINISTRATOR,
    EDITORIAL_CONTENT_OFFICER
}

enum class ParticipantStatus {
    ACTIVE,
    SUSPENDED,
    REMOVED
}

enum class SchemeStatus {
    DRAFT,
    IN_REVIEW,
    PUBLISHED,
    ARCHIVED
}

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // CHIEF_ADMINISTRATOR, EDITORIAL_CONTENT_OFFICER
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "government_schemes")
data class GovernmentSchemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nodalAgency: String,
    val sector: String,
    val description: String,
    val eligibility: String,
    val eligibleBusinesses: String, // Comma separated list
    val benefit: String,
    val ownContributionPct: Double = 15.0,
    val projectCostLimit: Double = 2500000.0,
    val requiredDocuments: String,
    val officialSource: String,
    val version: Int = 1,
    val status: String = "PUBLISHED", // DRAFT, IN_REVIEW, PUBLISHED, ARCHIVED
    val effectiveDate: String = "2026-01-01",
    val reviewDate: String = "2026-12-31"
)

@Entity(tableName = "evidence_sources")
data class EvidenceSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val organization: String,
    val category: String,
    val url: String,
    val reliabilityScore: Int = 90,
    val verificationStatus: String = "VERIFIED", // VERIFIED, REQUIRES_REVIEW, ESTIMATED, ARCHIVED
    val lastVerified: Long = System.currentTimeMillis(),
    val reviewer: String = "Editorial Officer"
)

@Entity(tableName = "translation_entries")
data class TranslationEntryEntity(
    @PrimaryKey val key: String,
    val en: String,
    val hi: String,
    val mr: String,
    val te: String,
    val kn: String,
    val status: String = "COMPLETE", // COMPLETE, PARTIAL, MISSING, REVIEW_REQUIRED
    val updatedBy: String = "system",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val adminUserId: String,
    val role: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val reason: String = ""
)

@Entity(tableName = "business_templates")
data class BusinessTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val requiredInputs: String,
    val marketFactors: String,
    val financialFactors: String,
    val riskFactors: String,
    val status: String = "ACTIVE"
)
