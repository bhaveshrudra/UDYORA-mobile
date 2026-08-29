package com.example.services

import com.example.types.BusinessTypeEnum
import com.example.types.FinancialOutput

data class FinancialScoringResult(
    val score: Int,                       // 0 to 100
    val status: String,                   // STRONG, ADEQUATE, CONDITIONAL, WEAK, INSUFFICIENT DATA
    val confidencePercent: Int,          // 0 to 100%
    val capitalAdequacyScore: Int,       // 0 to 100
    val dscrScore: Int,                  // 0 to 100
    val emiBurdenScore: Int,             // 0 to 100
    val surplusScore: Int                // 0 to 100
)

object FinancialScoringEngine {

    /**
     * Pure Deterministic Financial Scoring Engine.
     * Guaranteed: Same FinancialOutput -> Same FinancialScoringResult.
     */
    fun calculateScore(financials: FinancialOutput): FinancialScoringResult {
        if (financials.confidencePercent < 50 || financials.financialStatus == "INSUFFICIENT DATA") {
            return FinancialScoringResult(
                score = 0,
                status = "INSUFFICIENT DATA",
                confidencePercent = financials.confidencePercent,
                capitalAdequacyScore = 0,
                dscrScore = 0,
                emiBurdenScore = 0,
                surplusScore = 0
            )
        }

        // 1. Capital & Equity Ratio Score (0 to 100)
        val capScore = when {
            financials.equityRatio >= 0.50 -> 95
            financials.equityRatio >= 0.30 -> 80
            financials.equityRatio >= 0.20 -> 65
            financials.equityRatio >= 0.10 -> 45
            else -> 25
        }

        // 2. DSCR Score (0 to 100)
        val dscr = financials.dscr
        val dscrScore = when {
            dscr == null -> 0
            dscr >= 2.0 -> 95
            dscr >= 1.5 -> 85
            dscr >= 1.2 -> 70
            dscr >= 1.0 -> 50
            dscr >= 0.8 -> 30
            else -> 10
        }

        // 3. EMI Burden Ratio (EMI / Monthly Operating Surplus)
        val surplus = financials.operatingSurplusMonthly
        val emiBurdenScore = if (surplus != null && surplus > 0.0) {
            val emiRatio = financials.monthlyEmi / surplus
            when {
                emiRatio <= 0.20 -> 95
                emiRatio <= 0.35 -> 80
                emiRatio <= 0.50 -> 65
                emiRatio <= 0.75 -> 40
                else -> 15
            }
        } else {
            50
        }

        // 4. Operating Surplus Score
        val surplusScore = if (surplus != null) {
            when {
                surplus >= 30000.0 -> 90
                surplus >= 15000.0 -> 75
                surplus >= 8000.0 -> 60
                surplus > 0.0 -> 40
                else -> 10
            }
        } else {
            50
        }

        // Weighted Average Score: Cap (30%) + DSCR (35%) + EMI Burden (20%) + Surplus (15%)
        val compositeScore = (
            (capScore * 0.30) +
            (dscrScore * 0.35) +
            (emiBurdenScore * 0.20) +
            (surplusScore * 0.15)
        ).toInt().coerceIn(0, 100)

        val status = when {
            compositeScore >= 80 -> "STRONG"
            compositeScore >= 65 -> "ADEQUATE"
            compositeScore >= 45 -> "CONDITIONAL"
            else -> "WEAK"
        }

        return FinancialScoringResult(
            score = compositeScore,
            status = status,
            confidencePercent = financials.confidencePercent,
            capitalAdequacyScore = capScore,
            dscrScore = dscrScore,
            emiBurdenScore = emiBurdenScore,
            surplusScore = surplusScore
        )
    }
}
