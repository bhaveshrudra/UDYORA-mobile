package com.example.services

import com.example.data.AdminRole
import com.example.data.AdminUserEntity
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.data.GovernmentSchemeEntity

object AdminAuthService {

    var isAdminLoggedIn: Boolean = false

    var currentAdmin: AdminUserEntity = AdminUserEntity(
        id = "ADMIN-001",
        name = "Chief Administrator",
        email = "chief.admin@udyora.org",
        role = AdminRole.CHIEF_ADMINISTRATOR.name
    )

    fun isAuthenticatedAdmin(): Boolean = isAdminLoggedIn

    fun authenticateAdminPasscode(passcode: String): Boolean {
        if (passcode == "admin123" || passcode == "udyora2026") {
            isAdminLoggedIn = true
            currentAdmin = AdminUserEntity(
                id = "ADMIN-001",
                name = "Chief Administrator",
                email = "chief.admin@udyora.org",
                role = AdminRole.CHIEF_ADMINISTRATOR.name
            )
            return true
        }
        if (passcode == "editor123") {
            isAdminLoggedIn = true
            currentAdmin = AdminUserEntity(
                id = "ADMIN-002",
                name = "Editorial Officer",
                email = "editorial@udyora.org",
                role = AdminRole.EDITORIAL_CONTENT_OFFICER.name
            )
            return true
        }
        return false
    }

    fun logoutAdmin() {
        isAdminLoggedIn = false
    }

    fun isChiefAdmin(): Boolean = isAuthenticatedAdmin() && currentAdmin.role == AdminRole.CHIEF_ADMINISTRATOR.name
    fun isEditorialOfficer(): Boolean = isAuthenticatedAdmin() && currentAdmin.role == AdminRole.EDITORIAL_CONTENT_OFFICER.name

    suspend fun logAudit(
        db: AppDatabase,
        action: String,
        targetType: String,
        targetId: String,
        reason: String = ""
    ) {
        val entry = AuditLogEntity(
            adminUserId = currentAdmin.id,
            role = currentAdmin.role,
            action = action,
            targetType = targetType,
            targetId = targetId,
            reason = reason
        )
        db.auditLogDao().insertAuditLog(entry)
    }

    suspend fun suspendParticipant(db: AppDatabase, userId: String, reason: String) {
        if (!isChiefAdmin()) {
            throw SecurityException("Permission Denied: Only Chief Administrator can suspend participants.")
        }
        db.userDao().updateUserStatus(userId, "SUSPENDED")
        logAudit(db, "SUSPEND_PARTICIPANT", "User", userId, reason)
    }

    suspend fun reactivateParticipant(db: AppDatabase, userId: String, reason: String = "Reactivated by admin") {
        if (!isChiefAdmin()) {
            throw SecurityException("Permission Denied: Only Chief Administrator can reactivate participants.")
        }
        db.userDao().updateUserStatus(userId, "ACTIVE")
        logAudit(db, "REACTIVATE_PARTICIPANT", "User", userId, reason)
    }

    suspend fun removeParticipant(db: AppDatabase, userId: String, reason: String) {
        if (!isChiefAdmin()) {
            throw SecurityException("Permission Denied: Only Chief Administrator can remove participants.")
        }
        db.userDao().updateUserStatus(userId, "REMOVED")
        logAudit(db, "REMOVE_PARTICIPANT", "User", userId, reason)
    }

    suspend fun publishGovernmentScheme(db: AppDatabase, scheme: GovernmentSchemeEntity) {
        if (!isAuthenticatedAdmin()) {
            throw SecurityException("Permission Denied: Admin authentication required.")
        }
        if (scheme.officialSource.isBlank()) {
            throw IllegalArgumentException("Official source reference is required before publishing a government scheme.")
        }
        val updatedScheme = scheme.copy(status = "PUBLISHED")
        db.governmentSchemeDao().insertScheme(updatedScheme)
        logAudit(db, "PUBLISH_GOVERNMENT_SCHEME", "Scheme", scheme.id, "Published scheme ${scheme.name}")
    }
}
