package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.services.FinancialCalculator
import com.example.types.*
import com.example.utils.PdfReportGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Phase7ReportTest {

    private fun createTestSnapshot(): AssessmentInputSnapshot {
        val loc = CanonicalLocation(
            source = LocationSource.GPS,
            stateName = "Telangana",
            districtName = "Rangareddy",
            mandalName = "Shamshabad",
            pincode = "501218",
            latitude = 17.2543,
            longitude = 78.4356,
            verificationStatus = LocationVerificationStatus.VERIFIED
        )
        val bus = BusinessSnapshot(
            type = BusinessTypeEnum.DAIRY,
            description = "Rural dairy unit with 5 cows",
            dairyInputs = DairyInputs()
        )
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = bus.dairyInputs
        )
        return AssessmentInputSnapshot(
            assessmentRunId = "RUN-PHASE7-99",
            userId = "usr_9876543210",
            languageCode = "en",
            locationSnapshot = loc,
            businessSnapshot = bus,
            financialSnapshot = fin
        )
    }

    @Test
    fun testPdfReportFileGeneration() {
        val snapshot = createTestSnapshot()
        val pdfFile = PdfReportGenerator.generatePdfReport(
            context = ApplicationProvider.getApplicationContext(),
            snapshot = snapshot,
            participantName = "Rudra Varma",
            advisory = AdvisoryData(
                advisoryStatus = AdvisoryStatus.PROCEED,
                executiveSummary = "Strong dairy proposal",
                recommendedActions = listOf("Apply for NLM scheme"),
                nextSteps = listOf("Open business account")
            ),
            feasibilityScore = 82,
            feasibilityStatus = "HIGHLY FEASIBLE"
        )

        assertNotNull(pdfFile)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 0)
        assertEquals("UDYORA_Assessment_RUN-PHASE7-99.pdf", pdfFile.name)
    }

    @Test
    fun testAssessmentRunIdFilenameFormatting() {
        val snapshot = createTestSnapshot()
        val expectedFilename = "UDYORA_Assessment_${snapshot.assessmentRunId}.pdf"
        assertTrue(expectedFilename.contains("RUN-PHASE7-99"))
        assertFalse(expectedFilename.contains("mobile"))
    }

    @Test
    fun testFinancialOutputChartDataIntegrity() {
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = DairyInputs()
        )

        val totalPct = (fin.equityRatio * 100) + (fin.debtRatio * 100)
        assertEquals(100.0, totalPct, 0.01)
        assertTrue(fin.monthlyEmi > 0.0)
    }

    @Test
    fun testDataConfidenceSeparatedFromFeasibilityScore() {
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = DairyInputs()
        )

        val feasibilityScore = 54
        val confidencePercent = fin.confidencePercent

        assertNotEquals(feasibilityScore, confidencePercent)
        assertTrue(confidencePercent in 0..100)
    }
}
