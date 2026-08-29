package com.example.types

import kotlinx.serialization.Serializable

@Serializable
enum class AgentStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    PARTIAL,
    FAILED,
    INSUFFICIENT_DATA
}

@Serializable
enum class SourceType {
    OFFICIAL,
    GOVERNMENT,
    MAP,
    USER_PROVIDED,
    CALCULATED,
    ESTIMATED,
    MODEL_INFERENCE
}

@Serializable
enum class DataQuality {
    VERIFIED,
    ESTIMATED,
    DEMO,
    INSUFFICIENT_DATA
}

@Serializable
enum class AdvisoryStatus {
    PROCEED,
    PROCEED_WITH_CAUTION,
    CONDITIONAL,
    DO_NOT_PROCEED,
    INSUFFICIENT_DATA
}

@Serializable
enum class OpportunityStatus {
    UNSERVED,
    UNDERSERVED,
    SATURATED,
    INSUFFICIENT_DATA
}

@Serializable
data class LatLngData(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class AnalysisRequest(
    val requestId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val location: CanonicalLocation,
    val coordinates: LatLngData? = null,
    val businessType: BusinessTypeEnum,
    val businessDescription: String,
    val availableCapital: Double,
    val languageCode: String = "en"
)

@Serializable
data class EvidenceItem(
    val claim: String,
    val source: String,
    val value: String,
    val status: String = "VERIFIED", // VERIFIED, ESTIMATED, DEMO, INSUFFICIENT_DATA
    val confidence: Double = 0.85,
    val sourceType: SourceType = SourceType.MODEL_INFERENCE,
    val sourceUrl: String = "",
    val dataQuality: DataQuality = DataQuality.VERIFIED
)

@Serializable
data class AgentResult<T>(
    val agentId: String,
    val runId: String,
    val status: AgentStatus,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val data: T? = null,
    val errors: List<String> = emptyList(),
    val confidencePercent: Int = 0
)

@Serializable
data class BusinessAgentData(
    val businessType: String,
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val opportunities: List<String> = emptyList(),
    val threats: List<String> = emptyList(),
    val operationalConsiderations: List<String> = emptyList(),
    val requiredInputsList: List<String> = emptyList(),
    val confidence: Int = 80
)

@Serializable
data class CompetitorPoi(
    val name: String,
    val type: String,
    val distanceKm: Double,
    val source: String = "Map POI Registry",
    val dataQuality: DataQuality = DataQuality.VERIFIED
)

@Serializable
data class MarketAgentData(
    val catchmentKm: Double = 5.0,
    val populationReach: Int = 0,
    val populationDataQuality: DataQuality = DataQuality.VERIFIED,
    val marketDemandScore: Int = 65,
    val opportunityStatus: OpportunityStatus = OpportunityStatus.UNDERSERVED,
    val opportunityReason: String = "",
    val findings: List<String> = emptyList(),
    val demandDrivers: List<String> = emptyList(),
    val marketRisks: List<String> = emptyList(),
    val observedCompetitors: List<CompetitorPoi> = emptyList(),
    val competitorDataQuality: DataQuality = DataQuality.INSUFFICIENT_DATA,
    val productMarketValueText: String = "",
    val productMarketValueQuality: DataQuality = DataQuality.ESTIMATED,
    val confidence: Int = 75
)

@Serializable
data class FinancialAgentData(
    val availableCapital: Double,
    val psIndicativeProjectCost: Double,
    val psIndicativeLoanAmount: Double,
    val recommendedProjectCost: Double,
    val recommendedLoanAmount: Double,
    val recommendedMonthlyEmi: Double,
    val findings: List<String> = emptyList(),
    val financialRisks: List<String> = emptyList(),
    val financialStrengths: List<String> = emptyList(),
    val confidence: Int = 90
)

@Serializable
data class SchemeMatch(
    val schemeId: String = "",
    val schemeName: String,
    val matchStatus: String = "MATCHED", // MATCHED, PARTIAL_MATCH, NOT_MATCHED, REQUIRES_VERIFICATION
    val eligibilityStatus: String, // ELIGIBLE, POTENTIALLY_ELIGIBLE, REQUIRES_VERIFICATION
    val matchingConditions: List<String> = emptyList(),
    val potentialBenefit: String = "",
    val requiredDocuments: List<String> = emptyList(),
    val officialSource: String = "Government Portal",
    val sourceUrl: String = "",
    val lastVerified: String = "2026-08-28"
)

@Serializable
data class SchemeAgentData(
    val matchedSchemes: List<SchemeMatch> = emptyList(),
    val confidence: Int = 85
)

@Serializable
data class RiskItem(
    val riskName: String,
    val severity: String, // HIGH, MEDIUM, LOW
    val impact: String,
    val mitigation: String,
    val evidence: String = "",
    val confidence: Int = 80
)

@Serializable
data class RiskAgentData(
    val risks: List<RiskItem> = emptyList(),
    val overallRiskLevel: String = "MEDIUM",
    val confidence: Int = 80
)

@Serializable
data class EvidenceAgentData(
    val evidenceItems: List<EvidenceItem> = emptyList(),
    val verifiedCount: Int = 0,
    val estimatedCount: Int = 0,
    val insufficientDataCount: Int = 0,
    val overallDataConfidencePercent: Int = 85
)

@Serializable
data class AdvisoryData(
    val advisoryStatus: AdvisoryStatus,
    val executiveSummary: String,
    val keyStrengths: List<String> = emptyList(),
    val keyWeaknesses: List<String> = emptyList(),
    val opportunities: List<String> = emptyList(),
    val keyRisks: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val limitations: List<String> = emptyList()
)

@Serializable
data class AggregatedAssessment(
    val runId: String,
    val request: AnalysisRequest,
    val businessResult: AgentResult<BusinessAgentData>,
    val marketResult: AgentResult<MarketAgentData>,
    val financialResult: AgentResult<FinancialAgentData>,
    val schemeResult: AgentResult<SchemeAgentData>,
    val riskResult: AgentResult<RiskAgentData>,
    val evidenceResult: AgentResult<EvidenceAgentData>
)

@Serializable
data class AssessmentContext(
    val runId: String,
    val userName: String,
    val userLanguage: String,
    val locationSummary: String,
    val businessSummary: String,
    val ownCapital: Double,
    val psProjectCost: Double,
    val psLoanAmount: Double,
    val recommendedProjectCost: Double,
    val recommendedLoanAmount: Double,
    val monthlyEmi: Double,
    val dscr: Double?,
    val finalFeasibilityScore: Int,
    val dataConfidencePercent: Int,
    val feasibilityStatus: String,
    val advisoryStatus: AdvisoryStatus,
    val executiveSummary: String
)

@Serializable
data class AssessmentSession(
    val requestId: String,
    val languageCode: String = "en",
    val location: CanonicalLocation,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val district: String = "",
    val state: String = "",
    val businessType: BusinessTypeEnum,
    val businessDescription: String = "",
    val availableCapital: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING",
    val financialOutput: FinancialOutput? = null,
    val feasibilityScore: Int = 0,
    val dataConfidence: Int = 0,
    val swotStrengths: List<String> = emptyList(),
    val swotWeaknesses: List<String> = emptyList(),
    val swotOpportunities: List<String> = emptyList(),
    val swotThreats: List<String> = emptyList(),
    val marketData: MarketAgentData? = null,
    val financialData: FinancialAgentData? = null,
    val schemeData: SchemeAgentData? = null,
    val riskData: RiskAgentData? = null,
    val evidenceData: EvidenceAgentData? = null,
    val advisoryData: AdvisoryData? = null
)
