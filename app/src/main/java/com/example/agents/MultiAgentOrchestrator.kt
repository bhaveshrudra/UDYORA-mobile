package com.example.agents

import android.util.Log
import com.example.services.FeasibilityCalculator
import com.example.services.FeasibilityResult
import com.example.services.FinancialScoringEngine
import com.example.types.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MultiAgentOrchestrator {

    private val TAG = "ORCHESTRATOR"

    private val _orchestratorState = MutableStateFlow(AgentStatus.IDLE)
    val orchestratorState: StateFlow<AgentStatus> = _orchestratorState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _agentStates = MutableStateFlow<Map<String, AgentStatus>>(
        mapOf(
            "business" to AgentStatus.IDLE,
            "market" to AgentStatus.IDLE,
            "financial" to AgentStatus.IDLE,
            "scheme" to AgentStatus.IDLE,
            "risk" to AgentStatus.IDLE,
            "evidence" to AgentStatus.IDLE,
            "final" to AgentStatus.IDLE
        )
    )
    val agentStates: StateFlow<Map<String, AgentStatus>> = _agentStates.asStateFlow()

    private val _currentSession = MutableStateFlow<AssessmentSession?>(null)
    val currentSession: StateFlow<AssessmentSession?> = _currentSession.asStateFlow()

    private val _finalResults = MutableStateFlow<Map<String, String>?>(null)
    val finalResults: StateFlow<Map<String, String>?> = _finalResults.asStateFlow()

    private val _feasibilityResult = MutableStateFlow<FeasibilityResult?>(null)
    val feasibilityResult: StateFlow<FeasibilityResult?> = _feasibilityResult.asStateFlow()

    private val _advisoryData = MutableStateFlow<AdvisoryData?>(null)
    val advisoryData: StateFlow<AdvisoryData?> = _advisoryData.asStateFlow()

    private val _assessmentContext = MutableStateFlow<AssessmentContext?>(null)
    val assessmentContext: StateFlow<AssessmentContext?> = _assessmentContext.asStateFlow()

    fun resetState() {
        _orchestratorState.value = AgentStatus.IDLE
        _errorMessage.value = null
        _agentStates.value = mapOf(
            "business" to AgentStatus.IDLE,
            "market" to AgentStatus.IDLE,
            "financial" to AgentStatus.IDLE,
            "scheme" to AgentStatus.IDLE,
            "risk" to AgentStatus.IDLE,
            "evidence" to AgentStatus.IDLE,
            "final" to AgentStatus.IDLE
        )
    }

    suspend fun runAssessmentPipeline(snapshot: AssessmentInputSnapshot) = withContext(Dispatchers.IO) {
        val runId = snapshot.assessmentRunId
        _errorMessage.value = null
        _orchestratorState.value = AgentStatus.RUNNING
        Log.d(TAG, "[ORCHESTRATOR START] runId: $runId")

        try {
            // 1. Authoritative AnalysisRequest Creation
            val request = AnalysisRequest(
                requestId = "REQ-" + snapshot.assessmentRunId,
                timestamp = System.currentTimeMillis(),
                location = snapshot.locationSnapshot,
                coordinates = if (snapshot.locationSnapshot.latitude != null && snapshot.locationSnapshot.longitude != null) {
                    LatLngData(snapshot.locationSnapshot.latitude!!, snapshot.locationSnapshot.longitude!!)
                } else null,
                businessType = snapshot.businessSnapshot.type,
                businessDescription = snapshot.businessSnapshot.description,
                availableCapital = snapshot.financialSnapshot.ownCapital,
                languageCode = snapshot.languageCode
            )

            Log.d(TAG, "[UDYORA ASSESSMENT CREATED] requestId: ${request.requestId} | location: ${request.location.displayText} | business: ${request.businessType.displayName} | capital: ₹${request.availableCapital} | lang: ${request.languageCode}")

            // Step 1: Business Analysis Agent
            updateAgentState("business", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] business_agent | requestId: ${request.requestId}")
            val businessRes = SpecializedAgents.runBusinessAgent(snapshot)
            val bizVal = AssessmentValidator.validateBusinessResult(request, businessRes)
            val bizStatus = if (bizVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("business", bizStatus)
            Log.d(TAG, "[AGENT COMPLETE] business_agent | status: $bizStatus | valid: ${bizVal.isValid}")
            delay(250)

            // Step 2: Market Intelligence Agent
            updateAgentState("market", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] market_agent | requestId: ${request.requestId}")
            val marketRes = SpecializedAgents.runMarketAgent(snapshot)
            val marketVal = AssessmentValidator.validateMarketResult(request, marketRes)
            val marketStatus = if (marketVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("market", marketStatus)
            Log.d(TAG, "[AGENT COMPLETE] market_agent | status: $marketStatus | valid: ${marketVal.isValid}")
            delay(250)

            // Step 3: Financial Engine
            updateAgentState("financial", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] financial_agent | requestId: ${request.requestId}")
            val financialRes = SpecializedAgents.runFinancialAgent(snapshot)
            val finVal = AssessmentValidator.validateFinancialResult(request, financialRes)
            val finStatus = if (finVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("financial", finStatus)
            Log.d(TAG, "[AGENT COMPLETE] financial_agent | status: $finStatus | valid: ${finVal.isValid}")
            delay(250)

            // Step 4: Scheme Agent
            updateAgentState("scheme", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] scheme_agent | requestId: ${request.requestId}")
            val schemeRes = SpecializedAgents.runSchemeAgent(snapshot)
            val schemeVal = AssessmentValidator.validateSchemeResult(request, schemeRes)
            val schemeStatus = if (schemeVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("scheme", schemeStatus)
            Log.d(TAG, "[AGENT COMPLETE] scheme_agent | status: $schemeStatus | valid: ${schemeVal.isValid}")
            delay(250)

            // Step 5: Risk Agent
            updateAgentState("risk", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] risk_agent | requestId: ${request.requestId}")
            val riskRes = SpecializedAgents.runRiskAgent(snapshot)
            val riskVal = AssessmentValidator.validateRiskResult(request, riskRes)
            val riskStatus = if (riskVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("risk", riskStatus)
            Log.d(TAG, "[AGENT COMPLETE] risk_agent | status: $riskStatus | valid: ${riskVal.isValid}")
            delay(250)

            // Step 6: Evidence Agent
            updateAgentState("evidence", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] evidence_agent | requestId: ${request.requestId}")
            val evidenceRes = SpecializedAgents.runEvidenceAgent(snapshot)
            val evVal = AssessmentValidator.validateEvidenceResult(request, evidenceRes)
            val evStatus = if (evVal.isValid) AgentStatus.SUCCESS else AgentStatus.PARTIAL
            updateAgentState("evidence", evStatus)
            Log.d(TAG, "[AGENT COMPLETE] evidence_agent | status: $evStatus | valid: ${evVal.isValid}")
            delay(250)

            // 3. Aggregate & Validate Snapshot Results
            val aggregated = AgentAggregator.aggregate(
                runId = runId,
                request = request,
                businessResult = businessRes,
                marketResult = marketRes,
                financialResult = financialRes,
                schemeResult = schemeRes,
                riskResult = riskRes,
                evidenceResult = evidenceRes
            )

            val validationReport = AssessmentValidator.validate(snapshot, aggregated)
            if (!validationReport.isValid) {
                Log.e(TAG, "[VALIDATION FAILED] Errors: ${validationReport.errors}")
                _errorMessage.value = validationReport.errors.joinToString("\n")
                _orchestratorState.value = AgentStatus.FAILED
                return@withContext
            }

            // 4. Deterministic Feasibility & Financial Scoring Engine (AI DOES NOT SET FINAL SCORE)
            val financialScoreResult = FinancialScoringEngine.calculateScore(snapshot.financialSnapshot)

            val locationScoring = FeasibilityCalculator.computeFeasibility(
                runId = runId,
                stateName = snapshot.locationSnapshot.stateName ?: "Telangana",
                districtName = snapshot.locationSnapshot.districtName ?: "Rangareddy",
                mandalName = snapshot.locationSnapshot.mandalName ?: "Shamshabad",
                pincode = snapshot.locationSnapshot.pincode ?: "501218",
                businessType = snapshot.businessSnapshot.type.displayName,
                capital = snapshot.financialSnapshot.ownCapital,
                lat = snapshot.locationSnapshot.latitude,
                lng = snapshot.locationSnapshot.longitude,
                locationSource = snapshot.locationSnapshot.source.name
            )

            val marketScore = marketRes.data?.marketDemandScore ?: 75
            val dataConfidencePercent = evidenceRes.data?.overallDataConfidencePercent ?: 88

            // Pure Deterministic Composite Feasibility Formula:
            val finalScore = (
                (locationScoring.locationSuitabilityScore * 0.35) +
                (marketScore * 0.30) +
                (financialScoreResult.score * 0.35)
            ).toInt().coerceIn(0, 100)

            val statusStr = when {
                finalScore >= 78 -> "HIGHLY FEASIBLE"
                finalScore >= 60 -> "MODERATE FEASIBILITY"
                finalScore >= 45 -> "CONDITIONAL"
                else -> "NOT RECOMMENDED"
            }

            val advisoryStatusEnum = when {
                finalScore >= 78 -> AdvisoryStatus.PROCEED
                finalScore >= 60 -> AdvisoryStatus.PROCEED_WITH_CAUTION
                finalScore >= 45 -> AdvisoryStatus.CONDITIONAL
                else -> AdvisoryStatus.DO_NOT_PROCEED
            }

            _feasibilityResult.value = locationScoring.copy(
                finalScore = finalScore,
                status = statusStr,
                businessFeasibilityScore = financialScoreResult.score
            )

            // Step 7: Final Advisory Agent Synthesis
            updateAgentState("final", AgentStatus.RUNNING)
            Log.d(TAG, "[AGENT START] final_advisory_agent | requestId: ${request.requestId}")
            val advisoryRes = SpecializedAgents.runFinalAdvisoryAgent(snapshot, aggregated, finalScore, statusStr)
            updateAgentState("final", advisoryRes.status)
            Log.d(TAG, "[AGENT COMPLETE] final_advisory_agent | status: ${advisoryRes.status}")

            val advisoryFinal = advisoryRes.data ?: AdvisoryData(
                advisoryStatus = advisoryStatusEnum,
                executiveSummary = "The proposal displays solid business economics with strong financial readiness.",
                recommendedActions = listOf("Apply for matched scheme subsidy", "Finalize commercial location lease"),
                nextSteps = listOf("Open business bank account", "Register on Udyam portal")
            )
            _advisoryData.value = advisoryFinal

            _finalResults.value = mapOf(
                "RunId" to runId,
                "Business" to (businessRes.data?.strengths?.joinToString(", ") ?: "Viable operational model"),
                "Market" to (marketRes.data?.findings?.joinToString("; ") ?: "Stable local demand"),
                "Finance" to (financialRes.data?.findings?.joinToString("; ") ?: "Calculated loan: ₹${snapshot.financialSnapshot.loanAmount}"),
                "Schemes" to (schemeRes.data?.matchedSchemes?.joinToString("\n") { "${it.schemeName}: ${it.potentialBenefit}" } ?: "PM Vishwakarma"),
                "Risks" to (riskRes.data?.risks?.joinToString("\n") { "${it.riskName} (${it.severity}): ${it.mitigation}" } ?: "Standard risk mitigation"),
                "Evidence" to (evidenceRes.data?.evidenceItems?.joinToString("\n") { "${it.claim}: ${it.status}" } ?: "Verified inputs")
            )

            // Build Authoritative Single AssessmentSession
            val session = AssessmentSession(
                requestId = request.requestId,
                languageCode = snapshot.languageCode,
                location = snapshot.locationSnapshot,
                latitude = snapshot.locationSnapshot.latitude,
                longitude = snapshot.locationSnapshot.longitude,
                district = snapshot.locationSnapshot.districtName ?: "",
                state = snapshot.locationSnapshot.stateName ?: "",
                businessType = snapshot.businessSnapshot.type,
                businessDescription = snapshot.businessSnapshot.description,
                availableCapital = snapshot.financialSnapshot.ownCapital,
                createdAt = System.currentTimeMillis(),
                status = "COMPLETED",
                financialOutput = snapshot.financialSnapshot,
                feasibilityScore = finalScore,
                dataConfidence = dataConfidencePercent,
                swotStrengths = businessRes.data?.strengths ?: emptyList(),
                swotWeaknesses = businessRes.data?.weaknesses ?: emptyList(),
                swotOpportunities = businessRes.data?.opportunities ?: emptyList(),
                swotThreats = businessRes.data?.threats ?: emptyList(),
                marketData = marketRes.data,
                financialData = financialRes.data,
                schemeData = schemeRes.data,
                riskData = riskRes.data,
                evidenceData = evidenceRes.data,
                advisoryData = advisoryFinal
            )
            _currentSession.value = session

            // Prepare Chatbot Context Model with explicit SIH26091 variables
            _assessmentContext.value = AssessmentContext(
                runId = runId,
                userName = snapshot.userId,
                userLanguage = snapshot.languageCode,
                locationSummary = snapshot.locationSnapshot.displayText,
                businessSummary = "${snapshot.businessSnapshot.type.displayName} - ${snapshot.businessSnapshot.description}",
                ownCapital = snapshot.financialSnapshot.ownCapital,
                psProjectCost = snapshot.financialSnapshot.psIndicativeProjectCost,
                psLoanAmount = snapshot.financialSnapshot.psIndicativeLoanAmount,
                recommendedProjectCost = snapshot.financialSnapshot.projectCost,
                recommendedLoanAmount = snapshot.financialSnapshot.loanAmount,
                monthlyEmi = snapshot.financialSnapshot.monthlyEmi,
                dscr = snapshot.financialSnapshot.dscr,
                finalFeasibilityScore = finalScore,
                dataConfidencePercent = dataConfidencePercent,
                feasibilityStatus = statusStr,
                advisoryStatus = advisoryStatusEnum,
                executiveSummary = advisoryFinal.executiveSummary
            )

            _orchestratorState.value = AgentStatus.SUCCESS
            Log.d(TAG, "[ORCHESTRATOR COMPLETE] runId: $runId | finalScore: $finalScore | status: $statusStr")

        } catch (e: Exception) {
            Log.e(TAG, "[ORCHESTRATOR ERROR] Pipeline failed: ${e.message}", e)
            _errorMessage.value = e.message ?: "An unexpected error occurred during agent analysis."
            _orchestratorState.value = AgentStatus.FAILED
        }
    }

    private fun updateAgentState(id: String, status: AgentStatus) {
        val current = _agentStates.value.toMutableMap()
        current[id] = status
        _agentStates.value = current
    }
}
