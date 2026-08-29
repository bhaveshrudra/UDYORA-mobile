package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.AdminRole
import com.example.data.AdminUserEntity
import com.example.data.AppDatabase
import com.example.data.GovernmentSchemeEntity
import com.example.data.UserEntity
import com.example.services.AdminAuthService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Phase8AdminTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testChiefAdminCanSuspendParticipant() = runBlocking {
        AdminAuthService.currentAdmin = AdminUserEntity(
            id = "ADMIN-001",
            name = "Chief Administrator",
            email = "chief@udyora.org",
            role = AdminRole.CHIEF_ADMINISTRATOR.name
        )

        val user = UserEntity(
            id = "USR-P8-101",
            name = "Rudra",
            mobile = "9876543210",
            email = "rudra@udyora.org",
            languageCode = "en"
        )
        db.userDao().insertUser(user)

        AdminAuthService.suspendParticipant(db, "USR-P8-101", "Compliance Violation")

        val updatedUser = db.userDao().getUserById("USR-P8-101")
        assertNotNull(updatedUser)
        assertEquals("SUSPENDED", updatedUser!!.status)

        val auditLogs = db.auditLogDao().getAllAuditLogs().firstOrNull() ?: emptyList()
        assertTrue(auditLogs.any { it.action == "SUSPEND_PARTICIPANT" && it.targetId == "USR-P8-101" })
    }

    @Test
    fun testEditorialOfficerBlockedFromSuspendingParticipant() = runBlocking {
        AdminAuthService.currentAdmin = AdminUserEntity(
            id = "ADMIN-002",
            name = "Editorial Officer",
            email = "editor@udyora.org",
            role = AdminRole.EDITORIAL_CONTENT_OFFICER.name
        )

        val user = UserEntity(
            id = "USR-P8-102",
            name = "Anil",
            mobile = "9876543211",
            email = "anil@udyora.org",
            languageCode = "te"
        )
        db.userDao().insertUser(user)

        try {
            AdminAuthService.suspendParticipant(db, "USR-P8-102", "Unauthorized Attempt")
            fail("Expected SecurityException when Editorial Officer attempts suspension.")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Permission Denied"))
        }
    }

    @Test
    fun testOfficialSourceRequiredToPublishScheme() = runBlocking {
        val invalidScheme = GovernmentSchemeEntity(
            id = "SCH-001",
            name = "National Livestock Mission",
            nodalAgency = "Ministry of Fisheries & Animal Husbandry",
            sector = "Agriculture / Dairy",
            description = "Capital subsidy scheme",
            eligibility = "Farmers with land",
            eligibleBusinesses = "Dairy",
            benefit = "33% Subsidy",
            requiredDocuments = "Aadhaar, Land Records",
            officialSource = "", // Blank source!
            status = "DRAFT"
        )

        try {
            AdminAuthService.publishGovernmentScheme(db, invalidScheme)
            fail("Expected IllegalArgumentException when officialSource is blank.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Official source reference is required"))
        }
    }

    @Test
    fun testPublishValidSchemeCreatesAuditLog() = runBlocking {
        val validScheme = GovernmentSchemeEntity(
            id = "SCH-002",
            name = "PM Vishwakarma",
            nodalAgency = "Ministry of MSME",
            sector = "Artisans & Garments",
            description = "Subsidized loans and collateral-free credit",
            eligibility = "Artisans & Tailors",
            eligibleBusinesses = "Tailoring",
            benefit = "₹3 Lakh Loan @ 5%",
            requiredDocuments = "Aadhaar, Skill Card",
            officialSource = "https://pmvishwakarma.gov.in",
            status = "DRAFT"
        )

        AdminAuthService.publishGovernmentScheme(db, validScheme)

        val auditLogs = db.auditLogDao().getAllAuditLogs().firstOrNull() ?: emptyList()
        assertTrue(auditLogs.any { it.action == "PUBLISH_GOVERNMENT_SCHEME" && it.targetId == "SCH-002" })
    }
}
