package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users ORDER BY createdAt DESC")
    fun getAllAdminUsers(): Flow<List<AdminUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminUser(admin: AdminUserEntity)

    @Query("UPDATE admin_users SET status = :status WHERE id = :id")
    suspend fun updateAdminStatus(id: String, status: String)
}

@Dao
interface GovernmentSchemeDao {
    @Query("SELECT * FROM government_schemes ORDER BY name ASC")
    fun getAllSchemes(): Flow<List<GovernmentSchemeEntity>>

    @Query("SELECT * FROM government_schemes WHERE status = 'PUBLISHED'")
    suspend fun getPublishedSchemes(): List<GovernmentSchemeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheme(scheme: GovernmentSchemeEntity)

    @Query("UPDATE government_schemes SET status = :status, version = version + 1 WHERE id = :id")
    suspend fun updateSchemeStatus(id: String, status: String)
}

@Dao
interface EvidenceSourceDao {
    @Query("SELECT * FROM evidence_sources ORDER BY lastVerified DESC")
    fun getAllEvidenceSources(): Flow<List<EvidenceSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidenceSource(source: EvidenceSourceEntity)
}

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_entries ORDER BY key ASC")
    fun getAllTranslations(): Flow<List<TranslationEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntryEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface BusinessTemplateDao {
    @Query("SELECT * FROM business_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<BusinessTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: BusinessTemplateEntity)
}
