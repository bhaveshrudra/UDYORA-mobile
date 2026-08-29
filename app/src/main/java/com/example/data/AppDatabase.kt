package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AssessmentEntity::class,
        UserEntity::class,
        AdminUserEntity::class,
        GovernmentSchemeEntity::class,
        EvidenceSourceEntity::class,
        TranslationEntryEntity::class,
        AuditLogEntity::class,
        BusinessTemplateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assessmentDao(): AssessmentDao
    abstract fun userDao(): UserDao
    abstract fun adminUserDao(): AdminUserDao
    abstract fun governmentSchemeDao(): GovernmentSchemeDao
    abstract fun evidenceSourceDao(): EvidenceSourceDao
    abstract fun translationDao(): TranslationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun businessTemplateDao(): BusinessTemplateDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS admin_users (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        role TEXT NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS government_schemes (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        nodalAgency TEXT NOT NULL,
                        sector TEXT NOT NULL,
                        description TEXT NOT NULL,
                        eligibility TEXT NOT NULL,
                        eligibleBusinesses TEXT NOT NULL,
                        benefit TEXT NOT NULL,
                        ownContributionPct REAL NOT NULL,
                        projectCostLimit REAL NOT NULL,
                        requiredDocuments TEXT NOT NULL,
                        officialSource TEXT NOT NULL,
                        version INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        effectiveDate TEXT NOT NULL,
                        reviewDate TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS evidence_sources (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        organization TEXT NOT NULL,
                        category TEXT NOT NULL,
                        url TEXT NOT NULL,
                        reliabilityScore INTEGER NOT NULL,
                        verificationStatus TEXT NOT NULL,
                        lastVerified INTEGER NOT NULL,
                        reviewer TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS translation_entries (
                        key TEXT NOT NULL PRIMARY KEY,
                        en TEXT NOT NULL,
                        hi TEXT NOT NULL,
                        mr TEXT NOT NULL,
                        te TEXT NOT NULL,
                        kn TEXT NOT NULL,
                        status TEXT NOT NULL,
                        updatedBy TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        adminUserId TEXT NOT NULL,
                        role TEXT NOT NULL,
                        action TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        reason TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS business_templates (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        requiredInputs TEXT NOT NULL,
                        marketFactors TEXT NOT NULL,
                        financialFactors TEXT NOT NULL,
                        riskFactors TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "udyora_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
