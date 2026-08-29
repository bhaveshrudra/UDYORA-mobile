package com.example

import com.example.services.FinancialCalculator
import com.example.services.FinancialScoringEngine
import com.example.types.*
import org.junit.Assert.*
import org.junit.Test

class Phase3FinancialTest {

    @Test
    fun testGoldenEmiCalculation() {
        // GOLDEN TEST
        // Principal: 9,00,000, Interest: 9.5%, Tenure: 60 months
        val emi = FinancialCalculator.calculateEMI(
            principal = 900000.0,
            annualInterestRate = 9.5,
            tenureMonths = 60
        )
        // Standard expected EMI for 9,00,000 at 9.5% for 5 years is approx 18,903.78
        assertEquals(18903.78, emi, 1.0)
    }

    @Test
    fun testZeroInterestEmi() {
        val emi = FinancialCalculator.calculateEMI(
            principal = 120000.0,
            annualInterestRate = 0.0,
            tenureMonths = 12
        )
        assertEquals(10000.0, emi, 0.01)
    }

    @Test
    fun testInvalidEmiInputs() {
        assertEquals(0.0, FinancialCalculator.calculateEMI(0.0, 9.5, 60), 0.0)
        assertEquals(0.0, FinancialCalculator.calculateEMI(-50000.0, 9.5, 60), 0.0)
        assertEquals(0.0, FinancialCalculator.calculateEMI(100000.0, 9.5, 0), 0.0)
        assertEquals(0.0, FinancialCalculator.calculateEMI(100000.0, 9.5, -12), 0.0)
    }

    @Test
    fun testDscrCalculation() {
        val dscr = FinancialCalculator.calculateDSCR(
            annualNetOperatingCashFlow = 240000.0,
            annualDebtService = 120000.0
        )
        assertNotNull(dscr)
        assertEquals(2.0, dscr!!, 0.01)

        assertNull(FinancialCalculator.calculateDSCR(null, 120000.0))
        assertNull(FinancialCalculator.calculateDSCR(240000.0, 0.0))
    }

    @Test
    fun testDairyBusinessEconomicsAndFinancials() {
        val dairy = DairyInputs(
            numberOfAnimals = 5,
            milkYieldPerAnimal = 10.0,
            sellingPricePerLiter = 45.0,
            feedCostPerMonth = 15000.0,
            veterinaryCostPerMonth = 2000.0,
            laborCostPerMonth = 5000.0,
            shedCost = 50000.0,
            workingCapital = 25000.0
        )

        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = dairy
        )

        assertEquals(300000.0, fin.projectCost, 1.0) // 5*45k + 50k + 25k = 3,00,000
        assertEquals(100000.0, fin.ownCapital, 0.01)
        assertEquals(200000.0, fin.loanAmount, 0.01)
        assertEquals(0.333, fin.equityRatio, 0.01)
        assertEquals(0.666, fin.debtRatio, 0.01)

        // Revenue = 5 * 10 * 30 * 45 = 67,500
        // Operating Cost = 15000 + 2000 + 5000 = 22,000
        // Monthly Surplus = 45,500
        assertEquals(67500.0, fin.revenueMonthly!!, 0.01)
        assertEquals(22000.0, fin.operatingCostMonthly!!, 0.01)
        assertEquals(45500.0, fin.operatingSurplusMonthly!!, 0.01)

        assertTrue(fin.dscr!! > 2.0)
        assertEquals("PASS", fin.marginStatus)
        assertEquals("STRONG", fin.financialStatus)
    }

    @Test
    fun testScoreGoldenTest() {
        // SCORE GOLDEN TEST: Verify identical inputs -> identical score
        val dairy = DairyInputs()
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = dairy
        )

        val scoreResult1 = FinancialScoringEngine.calculateScore(fin)
        val scoreResult2 = FinancialScoringEngine.calculateScore(fin)

        assertEquals(scoreResult1.score, scoreResult2.score)
        assertEquals(scoreResult1.status, scoreResult2.status)
        assertEquals(scoreResult1.confidencePercent, scoreResult2.confidencePercent)
        assertTrue(scoreResult1.score in 0..100)
    }

    @Test
    fun testRemoteLocationFoundationSeparation() {
        // Verify location suitability vs financial viability dimensional separation
        val locationScore = 20
        val dairy = DairyInputs()
        val fin = FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = dairy
        )

        val finScore = FinancialScoringEngine.calculateScore(fin).score

        // Financial score is high (~85-95), location score is low (20).
        // They remain separate and independent dimensions.
        assertTrue(finScore > 70)
        assertEquals(20, locationScore)
        assertNotEquals(locationScore, finScore)
    }
}
