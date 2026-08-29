package com.example.agents

import com.example.types.*

object AgentAggregator {

    fun aggregate(
        runId: String,
        request: AnalysisRequest,
        businessResult: AgentResult<BusinessAgentData>,
        marketResult: AgentResult<MarketAgentData>,
        financialResult: AgentResult<FinancialAgentData>,
        schemeResult: AgentResult<SchemeAgentData>,
        riskResult: AgentResult<RiskAgentData>,
        evidenceResult: AgentResult<EvidenceAgentData>
    ): AggregatedAssessment {
        return AggregatedAssessment(
            runId = runId,
            request = request,
            businessResult = businessResult,
            marketResult = marketResult,
            financialResult = financialResult,
            schemeResult = schemeResult,
            riskResult = riskResult,
            evidenceResult = evidenceResult
        )
    }
}
