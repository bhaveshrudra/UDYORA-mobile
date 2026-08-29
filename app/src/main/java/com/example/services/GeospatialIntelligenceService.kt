package com.example.services

import com.example.types.*

object GeospatialIntelligenceService {

    /**
     * Discovers business-relevant nearby resources around (lat, lng) within maxRadiusMeters.
     */
    fun findNearbyResources(
        lat: Double,
        lng: Double,
        businessType: BusinessTypeEnum,
        maxRadiusMeters: Double
    ): List<NearbyResource> {
        val rawResources = generateCategoryResources(lat, lng, businessType)

        return rawResources
            .map { res ->
                val distMeters = GeoDistanceCalculator.calculateDistanceMeters(lat, lng, res.latitude, res.longitude)
                val distKm = (distMeters / 1000.0 * 100.0).let { Math.round(it) / 100.0 }
                res.copy(distanceKm = distKm)
            }
            .filter { res ->
                val distMeters = GeoDistanceCalculator.calculateDistanceMeters(lat, lng, res.latitude, res.longitude)
                distMeters <= maxRadiusMeters
            }
            .sortedBy { it.distanceKm }
            .distinctBy { "${it.name}_${it.latitude}_${it.longitude}" }
    }

    /**
     * Generates recommended opportunity locations nearby with deterministic scoring.
     */
    fun generateOpportunityLocations(
        lat: Double,
        lng: Double,
        businessType: BusinessTypeEnum,
        mandalName: String?
    ): List<RecommendedBusinessLocation> {
        val baseName = mandalName ?: "Local Market Hub"
        val candidates = listOf(
            Triple("$baseName Commercial Center", lat + 0.015, lng + 0.012),
            Triple("$baseName Highway Junction Hub", lat - 0.022, lng + 0.018),
            Triple("$baseName Agricultural Co-op Zone", lat + 0.028, lng - 0.015)
        )

        return candidates.mapIndexed { idx, (name, cLat, cLng) ->
            val distKm = GeoDistanceCalculator.calculateDistanceKm(lat, lng, cLat, cLng)
            val score = (88 - (idx * 6) - (distKm * 2).toInt()).coerceIn(45, 95)
            val reasons = when (businessType) {
                BusinessTypeEnum.DAIRY -> listOf("Proximity to main milk collection chilling center", "Direct highway connectivity for feed supply")
                BusinessTypeEnum.TAILORING -> listOf("High daily shopper footfall area", "Near major textile retail market")
                BusinessTypeEnum.KIRANA -> listOf("Dense residential household catchment", "Lower local retail competition density")
                BusinessTypeEnum.POULTRY_AGRO -> listOf("Good ventilation area with transport access", "Near regional feed supplier hub")
            }
            RecommendedBusinessLocation(
                id = "OPP-${idx + 1}",
                name = name,
                latitude = cLat,
                longitude = cLng,
                distanceKm = distKm,
                opportunityScore = score,
                reasons = reasons,
                businessType = businessType
            )
        }
    }

    private fun generateCategoryResources(
        lat: Double,
        lng: Double,
        businessType: BusinessTypeEnum
    ): List<NearbyResource> {
        return when (businessType) {
            BusinessTypeEnum.DAIRY -> listOf(
                NearbyResource("R-1", "Vijaya Dairy Milk Collection Center", "Milk Collection", lat + 0.008, lng + 0.005, 0.0, businessRelevance = "CRITICAL"),
                NearbyResource("R-2", "Government Veterinary Hospital", "Veterinary Healthcare", lat - 0.012, lng + 0.010, 0.0, businessRelevance = "HIGH"),
                NearbyResource("R-3", "Farmers Green Fodder Depot", "Feed & Fodder", lat + 0.018, lng - 0.015, 0.0, businessRelevance = "HIGH"),
                NearbyResource("R-4", "Nationalized Rural Bank Branch", "Financial Institution", lat + 0.025, lng + 0.020, 0.0, businessRelevance = "MEDIUM")
            )
            BusinessTypeEnum.TAILORING -> listOf(
                NearbyResource("R-1", "Central Garment Wholesale Market", "Textile & Material Supplier", lat + 0.006, lng + 0.004, 0.0, businessRelevance = "CRITICAL"),
                NearbyResource("R-2", "District Bus Station Commercial Complex", "High Footfall Hub", lat - 0.009, lng + 0.008, 0.0, businessRelevance = "HIGH"),
                NearbyResource("R-3", "Sewing Machine Machinery Repair Works", "Equipment Service", lat + 0.014, lng - 0.011, 0.0, businessRelevance = "HIGH")
            )
            BusinessTypeEnum.KIRANA -> listOf(
                NearbyResource("R-1", "APMC Grain & FMCG Wholesale Market", "Wholesale Inventory", lat + 0.010, lng + 0.012, 0.0, businessRelevance = "CRITICAL"),
                NearbyResource("R-2", "Town Bus Terminal Junction", "Transport & Footfall", lat - 0.007, lng + 0.005, 0.0, businessRelevance = "HIGH"),
                NearbyResource("R-3", "State Bank ATM & Micro-Branch", "Banking / Financial", lat + 0.004, lng - 0.003, 0.0, businessRelevance = "HIGH")
            )
            BusinessTypeEnum.POULTRY_AGRO -> listOf(
                NearbyResource("R-1", "Regional Poultry Feed Mill Depot", "Poultry Feed Supplier", lat + 0.020, lng + 0.015, 0.0, businessRelevance = "CRITICAL"),
                NearbyResource("R-2", "District Poultry Veterinary Clinic", "Healthcare", lat - 0.018, lng - 0.012, 0.0, businessRelevance = "HIGH"),
                NearbyResource("R-3", "State Highway Cold Storage Warehouse", "Logistics & Storage", lat + 0.035, lng + 0.025, 0.0, businessRelevance = "MEDIUM")
            )
        }
    }
}
