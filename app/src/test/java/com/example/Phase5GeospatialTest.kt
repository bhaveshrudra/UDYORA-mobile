package com.example

import com.example.services.GeoDistanceCalculator
import com.example.services.GeospatialIntelligenceService
import com.example.services.LocationSuitabilityEngine
import com.example.types.*
import org.junit.Assert.*
import org.junit.Test

class Phase5GeospatialTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Shamshabad (17.2543, 78.4356) and Hyderabad (17.3850, 78.4867) is approx 15.5 km
        val distMeters = GeoDistanceCalculator.calculateDistanceMeters(17.2543, 78.4356, 17.3850, 78.4867)
        val distKm = GeoDistanceCalculator.calculateDistanceKm(17.2543, 78.4356, 17.3850, 78.4867)

        assertTrue(distMeters > 14000.0 && distMeters < 17000.0)
        assertEquals(distMeters / 1000.0, distKm, 0.5)
    }

    @Test
    fun testRadiusFiltering5kmVs10km() {
        val lat = 17.2543
        val lng = 78.4356

        val resources5km = GeospatialIntelligenceService.findNearbyResources(lat, lng, BusinessTypeEnum.DAIRY, 5000.0)
        val resources10km = GeospatialIntelligenceService.findNearbyResources(lat, lng, BusinessTypeEnum.DAIRY, 10000.0)

        assertTrue(resources5km.isNotEmpty())
        assertTrue(resources10km.size >= resources5km.size)
        assertTrue(resources5km.all { it.distanceKm <= 5.0 })
        assertTrue(resources10km.all { it.distanceKm <= 10.0 })
    }

    @Test
    fun testBusinessSpecificResourcePrioritization() {
        val lat = 17.2543
        val lng = 78.4356

        val dairyRes = GeospatialIntelligenceService.findNearbyResources(lat, lng, BusinessTypeEnum.DAIRY, 10000.0)
        val kiranaRes = GeospatialIntelligenceService.findNearbyResources(lat, lng, BusinessTypeEnum.KIRANA, 10000.0)

        assertTrue(dairyRes.any { it.category == "Milk Collection" || it.category == "Veterinary Healthcare" })
        assertTrue(kiranaRes.any { it.category == "Wholesale Inventory" || it.category == "Transport & Footfall" })
    }

    @Test
    fun testOpportunityLocationGenerationAndRanking() {
        val lat = 17.2543
        val lng = 78.4356

        val opps = GeospatialIntelligenceService.generateOpportunityLocations(lat, lng, BusinessTypeEnum.DAIRY, "Shamshabad")

        assertTrue(opps.isNotEmpty())
        assertEquals(3, opps.size)
        assertTrue(opps.first().opportunityScore >= opps.last().opportunityScore)
        assertTrue(opps.first().opportunityScore in 0..100)
    }

    @Test
    fun testLocationSuitabilityEngine() {
        val loc = CanonicalLocation(
            source = LocationSource.GPS,
            stateName = "Telangana",
            districtName = "Rangareddy",
            mandalName = "Shamshabad",
            pincode = "501218",
            latitude = 17.2543,
            longitude = 78.4356,
            verificationStatus = LocationVerificationStatus.VERIFIED
        )
        val resources = GeospatialIntelligenceService.findNearbyResources(17.2543, 78.4356, BusinessTypeEnum.DAIRY, 5000.0)

        val result = LocationSuitabilityEngine.calculateSuitability(loc, resources, BusinessTypeEnum.DAIRY)

        assertTrue(result.locationSuitabilityScore in 0..100)
        assertFalse(result.isRemoteLocation)
        assertTrue(result.factorScores.isNotEmpty())
    }

    @Test
    fun testRemoteLocationSignalDetection() {
        val remoteLoc = CanonicalLocation(
            source = LocationSource.MANUAL,
            stateName = "Telangana",
            districtName = "Remote District",
            mandalName = "Remote Mandal",
            pincode = "500000",
            latitude = 15.0,
            longitude = 77.0,
            verificationStatus = LocationVerificationStatus.VERIFIED
        )
        val emptyResources = emptyList<NearbyResource>()

        val result = LocationSuitabilityEngine.calculateSuitability(remoteLoc, emptyResources, BusinessTypeEnum.DAIRY)

        assertTrue(result.isRemoteLocation)
        assertEquals("REMOTE", result.status)
        assertTrue(result.locationSuitabilityScore < 50)
    }
}
