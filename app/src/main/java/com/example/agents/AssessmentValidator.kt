package com.example.agents

import com.example.types.*

data class ValidationReport(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

object AssessmentValidator {

    fun validateBusinessResult(request: AnalysisRequest, result: AgentResult<BusinessAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Business agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Business agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.strengths.isEmpty()) {
            warnings.add("Business strengths list is empty")
        }
        if (data.weaknesses.isEmpty()) {
            warnings.add("Business weaknesses list is empty")
        }
        if (data.businessType.isBlank()) {
            errors.add("Business type is blank in business agent output")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validateMarketResult(request: AnalysisRequest, result: AgentResult<MarketAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Market agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Market agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.marketDemandScore !in 0..100) {
            errors.add("Market demand score out of bounds [0..100]: ${data.marketDemandScore}")
        }
        if (data.catchmentKm <= 0.0) {
            errors.add("Invalid catchment radius: ${data.catchmentKm} km")
        }
        if (data.populationReach < 0) {
            errors.add("Negative population reach: ${data.populationReach}")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validateFinancialResult(request: AnalysisRequest, result: AgentResult<FinancialAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Financial agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Financial agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.availableCapital != request.availableCapital) {
            errors.add("Financial available capital (${data.availableCapital}) does not match request capital (${request.availableCapital})")
        }
        if (data.psIndicativeProjectCost <= 0.0) {
            errors.add("Invalid PS indicative project cost: ${data.psIndicativeProjectCost}")
        }
        if (data.recommendedProjectCost <= 0.0) {
            errors.add("Invalid recommended project cost: ${data.recommendedProjectCost}")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validateSchemeResult(request: AnalysisRequest, result: AgentResult<SchemeAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Scheme agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Scheme agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.matchedSchemes.isEmpty()) {
            warnings.add("No government schemes matched for input criteria")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validateRiskResult(request: AnalysisRequest, result: AgentResult<RiskAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Risk agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Risk agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.risks.isEmpty()) {
            warnings.add("Risk assessment returned empty risk list")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validateEvidenceResult(request: AnalysisRequest, result: AgentResult<EvidenceAgentData>): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.status == AgentStatus.FAILED) {
            errors.add("Evidence agent status is FAILED")
            return ValidationReport(false, errors, warnings)
        }

        val data = result.data
        if (data == null) {
            errors.add("Evidence agent returned null data")
            return ValidationReport(false, errors, warnings)
        }

        if (data.evidenceItems.isEmpty()) {
            warnings.add("Evidence audit returned empty evidence list")
        }

        return ValidationReport(errors.isEmpty(), errors, warnings)
    }

    fun validate(snapshot: AssessmentInputSnapshot, aggregated: AggregatedAssessment): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (snapshot.assessmentRunId != aggregated.runId) {
            errors.add("Mismatch between snapshot runId (${snapshot.assessmentRunId}) and aggregated runId (${aggregated.runId})")
        }

        val fin = snapshot.financialSnapshot
        if (fin.projectCost <= 0.0) {
            errors.add("Invalid project cost: ₹${fin.projectCost}")
        }
        if (fin.ownCapital < 0.0) {
            errors.add("Invalid own capital: ₹${fin.ownCapital}")
        }
        if (fin.monthlyEmi < 0.0 || fin.monthlyEmi.isNaN()) {
            errors.add("Invalid calculated monthly EMI: ₹${fin.monthlyEmi}")
        }

        return ValidationReport(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
