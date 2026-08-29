package com.example.types

import kotlinx.serialization.Serializable

@Serializable
enum class BusinessTypeEnum(val displayName: String, val categoryKey: String) {
    DAIRY("Dairy Farming", "dairy"),
    TAILORING("Tailoring Unit", "tailoring"),
    KIRANA("Kirana Retail", "kirana"),
    POULTRY_AGRO("Poultry & Agro", "poultry");

    companion object {
        fun fromDisplayName(name: String?): BusinessTypeEnum {
            return values().find { it.displayName.equals(name, ignoreCase = true) } ?: DAIRY
        }
    }
}

@Serializable
data class DairyInputs(
    val numberOfAnimals: Int = 5,             // Required
    val milkYieldPerAnimal: Double = 10.0,    // L/day (Required)
    val sellingPricePerLiter: Double = 45.0,  // ₹/L (Required)
    val feedCostPerMonth: Double = 15000.0,   // ₹/mo
    val veterinaryCostPerMonth: Double = 2000.0, // ₹/mo
    val laborCostPerMonth: Double = 5000.0,   // ₹/mo
    val shedCost: Double = 50000.0,           // ₹
    val workingCapital: Double = 25000.0      // ₹
)

@Serializable
data class TailoringInputs(
    val numberOfMachines: Int = 3,            // Required
    val numberOfWorkers: Int = 2,             // Required
    val expectedMonthlyOrders: Int = 120,     // Required
    val avgOrderValue: Double = 350.0,        // ₹/order (Required)
    val materialCostPerMonth: Double = 12000.0,// ₹/mo
    val laborCostPerMonth: Double = 8000.0,   // ₹/mo
    val rentPerMonth: Double = 4000.0,        // ₹/mo
    val utilitiesPerMonth: Double = 1500.0    // ₹/mo
)

@Serializable
data class KiranaInputs(
    val initialInventoryCost: Double = 80000.0, // ₹ (Required)
    val shopSizeSqFt: Double = 200.0,          // SqFt
    val expectedCustomersPerDay: Int = 60,     // Required
    val avgBasketValue: Double = 200.0,        // ₹ (Required)
    val grossMarginPercent: Double = 15.0,     // % (Required)
    val rentPerMonth: Double = 5000.0,         // ₹/mo
    val utilitiesPerMonth: Double = 2000.0,    // ₹/mo
    val workingCapital: Double = 20000.0       // ₹
)

@Serializable
data class PoultryInputs(
    val birdCapacity: Int = 1000,              // Required
    val cycleDurationDays: Int = 45,           // Days per batch
    val expectedPricePerBird: Double = 140.0,  // ₹/bird (Required)
    val feedAndInputCost: Double = 85.0,       // ₹/bird
    val mortalityRatePercent: Double = 4.0,    // %
    val laborCostPerCycle: Double = 8000.0,    // ₹/cycle
    val infrastructureCost: Double = 100000.0, // ₹
    val workingCapital: Double = 30000.0       // ₹
)

@Serializable
data class BusinessSnapshot(
    val type: BusinessTypeEnum,
    val description: String,
    val dairyInputs: DairyInputs? = null,
    val tailoringInputs: TailoringInputs? = null,
    val kiranaInputs: KiranaInputs? = null,
    val poultryInputs: PoultryInputs? = null
)
