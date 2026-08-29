package com.example.types

import kotlinx.serialization.Serializable

@Serializable
data class CapitalDistribution(
    val ownEquity: Double,
    val debt: Double,
    val equityPercentage: Double,
    val debtPercentage: Double
)

@Serializable
data class FinancialOutput(
    val ownCapital: Double,
    val projectCost: Double,
    val loanAmount: Double,
    val equityRatio: Double,
    val debtRatio: Double,
    val monthlyEmi: Double,
    val annualDebtService: Double,
    val dscr: Double?, // Nullable if insufficient data
    val totalInterest: Double,
    val totalRepayment: Double,
    val marginStatus: String, // PASS, FAIL, INSUFFICIENT DATA
    val financialStatus: String, // STRONG, ADEQUATE, CONDITIONAL, WEAK, INSUFFICIENT DATA
    val confidencePercent: Int,
    val revenueMonthly: Double?,
    val operatingCostMonthly: Double?,
    val operatingSurplusMonthly: Double?,
    val breakEvenRevenueMonthly: Double?,
    val capitalDistribution: CapitalDistribution,
    val psIndicativeProjectCost: Double = 0.0,
    val psIndicativeLoanAmount: Double = 0.0
)
