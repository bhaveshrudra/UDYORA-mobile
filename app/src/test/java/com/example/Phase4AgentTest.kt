package com.example

import com.example.agents.AgentAggregator
import com.example.agents.AssessmentValidator
import com.example.agents.MultiAgentOrchestrator
import com.example.agents.SpecializedAgents
import com.example.services.FinancialCalculator
import com.example.types.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Phase4AgentTest {

    private fun createTestSnapshot(
        runId: String = "RUN-101",
        busType: BusinessTypeEnum = BusinessTypeEnum.DAIRY,
        ownCap: Double = 100000.0,
        stateName: String = "Telangana",
        mandalName: String = "Shamshabad"
    ): AssessmentInputSnapshot {
        val loc = CanonicalLocation(
            source = LocationSource.MANUAL,
            stateId = "TG",
            stateName = stateName,
            districtId = "TG_RR",
            districtName = "Rangareddy",
            mandalId = "TG_RR_SH",
            mandalName = mandalName,
            pincode = "501218",
            locality = "Shamshabad Area",
            latitude = 17.2543,
            longitude = 78.4356,
            verificationStatus = LocationVerificationStatus.VERIFIED
        )
        val bus = BusinessSnapshot(
            type = busType,
            description = "Rural dairy unit with 5 buffaloes",
            dairyInputs = DairyInputs()
        )
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = ownCap,
            businessType = busType,
            dairyInputs = bus.dairyInputs
        )
        return AssessmentInputSnapshot(
            assessmentRunId = runId,
            userId = "usr_9876543210",
            languageCode = "en",
            locationSnapshot = loc,
            businessSnapshot = bus,
            financialSnapshot = fin
        )
    }

    @Test
    fun testBusinessAgentExecution() = runBlocking {
        val snapshot = createTestSnapshot()
        val result = SpecializedAgents.runBusinessAgent(snapshot)

        assertEquals("business_agent", result.agentId)
        assertEquals("RUN-101", result.runId)
        assertNotNull(result.data)
        assertTrue(result.data!!.strengths.isNotEmpty())
        assertTrue(result.confidencePercent > 0)
    }

    @Test
    fun testMarketAgentExecution() = runBlocking {
        val snapshot = createTestSnapshot()
        val result = SpecializedAgents.runMarketAgent(snapshot)

        assertEquals("market_agent", result.agentId)
        assertNotNull(result.data)
        assertTrue(result.data!!.marketDemandScore in 0..100)
    }

    @Test
    fun testFinancialAgentDoesNotDoArithmetic() = runBlocking {
        val snapshot = createTestSnapshot()
        val result = SpecializedAgents.runFinancialAgent(snapshot)

        assertEquals("financial_agent", result.agentId)
        assertNotNull(result.data)
        // Financial agent explains deterministic math results
        assertTrue(result.data!!.findings.any { it.contains("Project Cost") })
    }

    @Test
    fun testSchemeAgentMatching() = runBlocking {
        val snapshot = createTestSnapshot(busType = BusinessTypeEnum.DAIRY)
        val result = SpecializedAgents.runSchemeAgent(snapshot)

        assertNotNull(result.data)
        assertTrue(result.data!!.matchedSchemes.any { it.schemeName.contains("National Livestock Mission") })
    }

    @Test
    fun testRiskAgentCategorySpecifics() = runBlocking {
        val snapshot = createTestSnapshot(busType = BusinessTypeEnum.KIRANA)
        val result = SpecializedAgents.runRiskAgent(snapshot)

        assertNotNull(result.data)
        assertTrue(result.data!!.risks.any { it.riskName.contains("Competition") })
    }

    @Test
    fun testEvidenceAgentValidation() = runBlocking {
        val snapshot = createTestSnapshot()
        val result = SpecializedAgents.runEvidenceAgent(snapshot)

        assertNotNull(result.data)
        assertTrue(result.data!!.evidenceItems.any { it.status == "VERIFIED" })
    }

    @Test
    fun testAggregatorAndValidator() = runBlocking {
        val snapshot = createTestSnapshot()
        val bRes = SpecializedAgents.runBusinessAgent(snapshot)
        val mRes = SpecializedAgents.runMarketAgent(snapshot)
        val fRes = SpecializedAgents.runFinancialAgent(snapshot)
        val sRes = SpecializedAgents.runSchemeAgent(snapshot)
        val rRes = SpecializedAgents.runRiskAgent(snapshot)
        val eRes = SpecializedAgents.runEvidenceAgent(snapshot)

        val aggregated = AgentAggregator.aggregate(snapshot.assessmentRunId, bRes, mRes, fRes, sRes, rRes, eRes)
        assertEquals("RUN-101", aggregated.runId)

        val valReport = AssessmentValidator.validate(snapshot, aggregated)
        assertTrue(valReport.isValid)
        assertTrue(valReport.errors.isEmpty())
    }

    @Test
    fun testGoldenScenarioPipelineExecution() = runBlocking {
        val snapshot = createTestSnapshot()
        val orchestrator = MultiAgentOrchestrator()
        orchestrator.runAssessmentPipeline(snapshot)

        assertEquals(AgentStatus.SUCCESS, orchestrator.orchestratorState.value)
        assertNotNull(orchestrator.feasibilityResult.value)
        val score = orchestrator.feasibilityResult.value!!.finalScore
        assertTrue(score in 0..100)
        assertNotNull(orchestrator.advisoryData.value)
        assertNotNull(orchestrator.assessmentContext.value)
    }

    @Test
    fun testDifferentCapitalProducesDifferentFinancialOutput() = runBlocking {
        val snapshot1 = createTestSnapshot(ownCap = 50000.0)
        val snapshot2 = createTestSnapshot(ownCap = 200000.0)

        assertNotEquals(snapshot1.financialSnapshot.loanAmount, snapshot2.financialSnapshot.loanAmount)
        assertNotEquals(snapshot1.financialSnapshot.monthlyEmi, snapshot2.financialSnapshot.monthlyEmi)
    }
}
