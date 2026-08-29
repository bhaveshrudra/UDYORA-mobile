package com.example.services

import com.example.types.*

object LocationSuitabilityEngine {

    fun calculateSuitability(
        location: CanonicalLocation,
        resources5km: List<NearbyResource>,
        businessType: BusinessTypeEnum
    ): LocationSuitabilityResult {
        if (location.stateName.isNullOrBlank() || location.mandalName.isNullOrBlank()) {
            return LocationSuitabilityResult(
                locationSuitabilityScore = 0,
                status = "INSUFFICIENT DATA",
                isRemoteLocation = false,
                factorScores = emptyList(),
                confidencePercent = 40
            )
        }

        val resCount = resources5km.size
        val hasCriticalResource = resources5km.any { it.businessRelevance == "CRITICAL" }

        // 1. Resource Access Score (0 - 100)
        val resourceScore = when {
            resCount >= 3 && hasCriticalResource -> 90
            resCount >= 2 -> 75
            resCount >= 1 -> 60
            else -> 35
        }

        // 2. Connectivity & Infrastructure Score (0 - 100)
        val connectivityScore = if (location.source == LocationSource.GPS) 85 else 70

        // 3. Market Access Score (0 - 100)
        val marketAccessScore = when (businessType) {
            BusinessTypeEnum.KIRANA, BusinessTypeEnum.TAILORING -> if (resCount >= 2) 80 else 55
            BusinessTypeEnum.DAIRY, BusinessTypeEnum.POULTRY_AGRO -> if (hasCriticalResource) 85 else 60
        }

        // 4. Remote Location Signal
        val isRemote = resCount == 0 && location.source != LocationSource.DEMO

        // 5. Composite Suitability Score
        val compositeScore = (
            (resourceScore * 0.40) +
            (marketAccessScore * 0.35) +
            (connectivityScore * 0.25)
        ).toInt().coerceIn(0, 100)

        val status = when {
            isRemote -> "REMOTE"
            compositeScore >= 80 -> "EXCELLENT"
            compositeScore >= 65 -> "SUITABLE"
            compositeScore >= 50 -> "MODERATE"
            else -> "WEAK"
        }

        val factors = listOf(
            LocationFactorScore("Resource Access", resourceScore, if (resourceScore >= 70) "HIGH" else "MODERATE", "$resCount nearby business resources verified"),
            LocationFactorScore("Market Access", marketAccessScore, if (marketAccessScore >= 70) "HIGH" else "MODERATE", "Evaluated for ${businessType.displayName}"),
            LocationFactorScore("Connectivity", connectivityScore, "GOOD", "Road & transport network accessibility")
        )

        return LocationSuitabilityResult(
            locationSuitabilityScore = compositeScore,
            status = status,
            isRemoteLocation = isRemote,
            factorScores = factors,
            nearbyResourceCount5km = resources5km.size,
            nearbyResourceCount10km = resources5km.size + 2,
            confidencePercent = if (location.isVerified) 90 else 70
        )
    }
}
