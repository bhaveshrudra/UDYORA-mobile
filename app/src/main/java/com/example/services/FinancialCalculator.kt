package com.example.services

import com.example.types.*

object FinancialCalculator {

    /**
     * Standard Reducing-Balance Monthly EMI Calculation.
     * Formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     * For r = 0: EMI = P / n
     */
    fun calculateEMI(
        principal: Double,
        annualInterestRate: Double,
        tenureMonths: Int
    ): Double {
        if (principal <= 0.0 || tenureMonths <= 0) return 0.0
        if (annualInterestRate <= 0.0) return principal / tenureMonths

        val r = (annualInterestRate / 100.0) / 12.0
        val n = tenureMonths.toDouble()
        val numerator = principal * r * Math.pow(1.0 + r, n)
        val denominator = Math.pow(1.0 + r, n) - 1.0

        if (denominator == 0.0) return 0.0
        return numerator / denominator
    }

    /**
     * Calculates Debt Service Coverage Ratio (DSCR).
     * Formula: DSCR = Net Operating Cash Flow (Annual) / Annual Debt Service
     */
    fun calculateDSCR(
        annualNetOperatingCashFlow: Double?,
        annualDebtService: Double
    ): Double? {
        if (annualNetOperatingCashFlow == null || annualDebtService <= 0.0) return null
        return annualNetOperatingCashFlow / annualDebtService
    }

    /**
     * Comprehensive Deterministic Financial Calculation Engine for SIH26091.
     * Keeps strictly separate:
     * A. PS-Based Financing Capacity (10% Margin / 90% Loan Rule)
     * B. Recommended Business Economics Project Size
     */
    fun calculateFinancials(
        ownCapitalInput: Double,
        businessType: BusinessTypeEnum,
        dairyInputs: DairyInputs? = null,
        tailoringInputs: TailoringInputs? = null,
        kiranaInputs: KiranaInputs? = null,
        poultryInputs: PoultryInputs? = null,
        annualInterestRate: Double = 9.5,
        tenureMonths: Int = 60,
        requiredMarginPercent: Double = 10.0 // SIH26091 10% promoter margin specification
    ): FinancialOutput {
        val ownCapital = ownCapitalInput.coerceAtLeast(0.0)

        // 1. SIH26091 PS-Based Financing Capacity (10% promoter equity, 90% loan)
        val psIndicativeProjectCost = if (ownCapital > 0.0) ownCapital / (requiredMarginPercent / 100.0) else 0.0
        val psIndicativeLoanAmount = if (ownCapital > 0.0) ownCapital * (1.0 - (requiredMarginPercent / 100.0)) else 0.0

        // 2. Business Economics Model Project Cost
        val (calculatedEconProjectCost, monthlyRevenue, monthlyOperatingCost, monthlySurplus, grossMarginRatio) = calculateBusinessEconomics(
            businessType, dairyInputs, tailoringInputs, kiranaInputs, poultryInputs
        )

        // Recommended Project Cost: use calculated economics if supplied, or default scale based on business type
        val defaultBusinessCost = when (businessType) {
            BusinessTypeEnum.DAIRY -> 450000.0
            BusinessTypeEnum.TAILORING -> 200000.0
            BusinessTypeEnum.KIRANA -> 350000.0
            BusinessTypeEnum.POULTRY_AGRO -> 500000.0
        }
        
        val recommendedProjectCost = if (calculatedEconProjectCost > 0.0) calculatedEconProjectCost else defaultBusinessCost
        val recommendedLoanAmount = (recommendedProjectCost - ownCapital).coerceAtLeast(0.0)
        val equityRatio = if (recommendedProjectCost > 0.0) (ownCapital / recommendedProjectCost).coerceIn(0.0, 1.0) else 1.0
        val debtRatio = if (recommendedProjectCost > 0.0) (recommendedLoanAmount / recommendedProjectCost).coerceIn(0.0, 1.0) else 0.0

        // 3. Recommended Loan EMI & Debt Service
        val monthlyEmi = calculateEMI(recommendedLoanAmount, annualInterestRate, tenureMonths)
        val annualDebtService = monthlyEmi * 12.0
        val totalRepayment = monthlyEmi * tenureMonths
        val totalInterest = (totalRepayment - recommendedLoanAmount).coerceAtLeast(0.0)

        // 4. DSCR Calculation
        val annualNetCashFlow = if (monthlySurplus != null) monthlySurplus * 12.0 else null
        val dscr = calculateDSCR(annualNetCashFlow, annualDebtService)

        // 5. Break-Even Revenue
        val breakEvenRevenueMonthly = if (monthlyOperatingCost != null && grossMarginRatio != null && grossMarginRatio > 0.0) {
            (monthlyOperatingCost + monthlyEmi) / grossMarginRatio
        } else null

        // 6. Margin Adequacy Check
        val requiredOwnContributionForRecCost = recommendedProjectCost * (requiredMarginPercent / 100.0)
        val marginStatus = if (ownCapital >= requiredOwnContributionForRecCost) "PASS" else "MARGIN_DEFICIT"

        // 7. Financial Status Determination
        val financialStatus = when {
            ownCapital <= 0.0 -> "INSUFFICIENT_CAPITAL"
            dscr != null && dscr >= 1.4 && marginStatus == "PASS" -> "STRONG"
            dscr != null && dscr >= 1.15 -> "ADEQUATE"
            marginStatus == "PASS" -> "CONDITIONAL"
            else -> "WEAK"
        }

        // 8. Confidence Percent
        val hasCustomInputs = when (businessType) {
            BusinessTypeEnum.DAIRY -> dairyInputs != null
            BusinessTypeEnum.TAILORING -> tailoringInputs != null
            BusinessTypeEnum.KIRANA -> kiranaInputs != null
            BusinessTypeEnum.POULTRY_AGRO -> poultryInputs != null
        }
        val confidencePercent = if (hasCustomInputs && ownCapital > 0.0) 95 else if (ownCapital > 0.0) 80 else 40

        // 9. Capital Distribution
        val capDist = CapitalDistribution(
            ownEquity = ownCapital,
            debt = recommendedLoanAmount,
            equityPercentage = equityRatio * 100.0,
            debtPercentage = debtRatio * 100.0
        )

        return FinancialOutput(
            ownCapital = ownCapital,
            projectCost = recommendedProjectCost,
            loanAmount = recommendedLoanAmount,
            equityRatio = equityRatio,
            debtRatio = debtRatio,
            monthlyEmi = monthlyEmi,
            annualDebtService = annualDebtService,
            dscr = dscr,
            totalInterest = totalInterest,
            totalRepayment = totalRepayment,
            marginStatus = marginStatus,
            financialStatus = financialStatus,
            confidencePercent = confidencePercent,
            revenueMonthly = monthlyRevenue,
            operatingCostMonthly = monthlyOperatingCost,
            operatingSurplusMonthly = monthlySurplus,
            breakEvenRevenueMonthly = breakEvenRevenueMonthly,
            capitalDistribution = capDist,
            psIndicativeProjectCost = psIndicativeProjectCost,
            psIndicativeLoanAmount = psIndicativeLoanAmount
        )
    }

    private data class BusinessEconomicsResult(
        val projectCost: Double,
        val monthlyRevenue: Double?,
        val monthlyOperatingCost: Double?,
        val monthlySurplus: Double?,
        val grossMarginRatio: Double?
    )

    private fun calculateBusinessEconomics(
        type: BusinessTypeEnum,
        dairy: DairyInputs?,
        tailoring: TailoringInputs?,
        kirana: KiranaInputs?,
        poultry: PoultryInputs?
    ): BusinessEconomicsResult {
        return when (type) {
            BusinessTypeEnum.DAIRY -> {
                if (dairy == null) return BusinessEconomicsResult(0.0, null, null, null, null)
                val cost = (dairy.numberOfAnimals * 45000.0) + dairy.shedCost + dairy.workingCapital
                val rev = dairy.numberOfAnimals * dairy.milkYieldPerAnimal * 30.0 * dairy.sellingPricePerLiter
                val opex = dairy.feedCostPerMonth + dairy.veterinaryCostPerMonth + dairy.laborCostPerMonth
                val surplus = rev - opex
                val marginRatio = if (rev > 0.0) surplus / rev else null
                BusinessEconomicsResult(cost, rev, opex, surplus, marginRatio)
            }
            BusinessTypeEnum.TAILORING -> {
                if (tailoring == null) return BusinessEconomicsResult(0.0, null, null, null, null)
                val cost = (tailoring.numberOfMachines * 25000.0) + 15000.0
                val rev = tailoring.expectedMonthlyOrders * tailoring.avgOrderValue
                val opex = tailoring.materialCostPerMonth + tailoring.laborCostPerMonth + tailoring.rentPerMonth + tailoring.utilitiesPerMonth
                val surplus = rev - opex
                val marginRatio = if (rev > 0.0) (rev - tailoring.materialCostPerMonth) / rev else null
                BusinessEconomicsResult(cost, rev, opex, surplus, marginRatio)
            }
            BusinessTypeEnum.KIRANA -> {
                if (kirana == null) return BusinessEconomicsResult(0.0, null, null, null, null)
                val cost = kirana.initialInventoryCost + kirana.workingCapital + 20000.0
                val rev = kirana.expectedCustomersPerDay * 30.0 * kirana.avgBasketValue
                val grossMarginAmt = rev * (kirana.grossMarginPercent / 100.0)
                val opex = kirana.rentPerMonth + kirana.utilitiesPerMonth
                val surplus = grossMarginAmt - opex
                val marginRatio = kirana.grossMarginPercent / 100.0
                BusinessEconomicsResult(cost, rev, opex, surplus, marginRatio)
            }
            BusinessTypeEnum.POULTRY_AGRO -> {
                if (poultry == null) return BusinessEconomicsResult(0.0, null, null, null, null)
                val cost = poultry.infrastructureCost + poultry.workingCapital
                val cyclesPerMonth = 30.0 / poultry.cycleDurationDays.toDouble().coerceAtLeast(1.0)
                val effectiveBirdsPerCycle = poultry.birdCapacity * (1.0 - (poultry.mortalityRatePercent / 100.0))
                val revPerCycle = effectiveBirdsPerCycle * poultry.expectedPricePerBird
                val opexPerCycle = (poultry.birdCapacity * poultry.feedAndInputCost) + poultry.laborCostPerCycle
                val rev = revPerCycle * cyclesPerMonth
                val opex = opexPerCycle * cyclesPerMonth
                val surplus = rev - opex
                val marginRatio = if (rev > 0.0) surplus / rev else null
                BusinessEconomicsResult(cost, rev, opex, surplus, marginRatio)
            }
        }
    }
}
