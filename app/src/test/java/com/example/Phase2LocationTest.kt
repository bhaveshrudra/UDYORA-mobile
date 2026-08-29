package com.example

import com.example.services.DemoScenario
import com.example.services.LocationHierarchyService
import com.example.types.CanonicalLocation
import com.example.types.GpsState
import com.example.types.LocationSource
import com.example.types.LocationVerificationStatus
import com.example.ui.screens.SharedViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Phase2LocationTest {

    private lateinit var viewModel: SharedViewModel

    @Before
    fun setup() {
        viewModel = SharedViewModel()
    }

    @Test
    fun testCanonicalLocationDefaults() {
        val loc = CanonicalLocation()
        assertEquals(LocationSource.MANUAL, loc.source)
        assertEquals(LocationVerificationStatus.UNVERIFIED, loc.verificationStatus)
        assertFalse(loc.isVerified)
        assertFalse(loc.isDemo)
        assertNull(loc.stateName)
        assertNull(loc.districtName)
        assertNull(loc.mandalName)
        assertNull(loc.pincode)
    }

    @Test
    fun testStateSelectionAndCascadingResets() {
        viewModel.selectState("TG", "Telangana")
        val loc = viewModel.currentLocation.value

        assertEquals("TG", loc.stateId)
        assertEquals("Telangana", loc.stateName)
        assertNull(loc.districtId)
        assertNull(loc.mandalId)
        assertNull(loc.pincode)
        assertFalse(loc.isVerified)

        viewModel.selectDistrict("TG_RR", "Rangareddy")
        val loc2 = viewModel.currentLocation.value
        assertEquals("TG_RR", loc2.districtId)
        assertEquals("Rangareddy", loc2.districtName)

        // Reset state
        viewModel.selectState("MH", "Maharashtra")
        val loc3 = viewModel.currentLocation.value
        assertEquals("MH", loc3.stateId)
        assertNull(loc3.districtId)
        assertNull(loc3.mandalId)
    }

    @Test
    fun testDistrictAndMandalFiltering() {
        val districts = LocationHierarchyService.searchDistricts("TG", "")
        assertTrue(districts.isNotEmpty())
        assertTrue(districts.any { it.districtName == "Rangareddy" })

        val mandals = LocationHierarchyService.searchMandals("TG", "TG_RR", "")
        assertTrue(mandals.isNotEmpty())
        assertTrue(mandals.any { it.mandalName == "Shamshabad" })

        val filtered = LocationHierarchyService.searchMandals("TG", "TG_RR", "Sham")
        assertEquals(1, filtered.size)
        assertEquals("Shamshabad", filtered.first().mandalName)
    }

    @Test
    fun testPincodeValidationAndResolution() {
        viewModel.selectState("TG", "Telangana")
        viewModel.selectDistrict("TG_RR", "Rangareddy")
        viewModel.selectMandal("TG_RR_SH", "Shamshabad", 17.2543, 78.4356, "Shamshabad Airport Area")

        viewModel.updatePincode("501218")
        val loc = viewModel.currentLocation.value
        assertEquals("501218", loc.pincode)
        assertTrue(loc.isVerified)

        viewModel.updatePincode("999999")
        val locMismatch = viewModel.currentLocation.value
        assertEquals(LocationVerificationStatus.INCONSISTENT, locMismatch.verificationStatus)
        assertFalse(locMismatch.isVerified)
    }

    @Test
    fun testPincodeAutoResolutionWhenParentEmpty() {
        viewModel.updatePincode("501218")
        val loc = viewModel.currentLocation.value
        assertEquals("TG", loc.stateId)
        assertEquals("Telangana", loc.stateName)
        assertEquals("TG_RR", loc.districtId)
        assertEquals("Rangareddy", loc.districtName)
        assertEquals("TG_RR_SH", loc.mandalId)
        assertEquals("Shamshabad", loc.mandalName)
        assertEquals(LocationSource.PINCODE, loc.source)
        assertTrue(loc.isVerified)
    }

    @Test
    fun testDemoModeIsolation() {
        viewModel.useDemoLocation()
        val loc = viewModel.currentLocation.value

        assertEquals(LocationSource.DEMO, loc.source)
        assertTrue(loc.isDemo)
        assertEquals(DemoScenario.STATE_NAME, loc.stateName)
        assertEquals(DemoScenario.DISTRICT_NAME, loc.districtName)
        assertEquals(DemoScenario.MANDAL_NAME, loc.mandalName)
        assertEquals(DemoScenario.PINCODE, loc.pincode)
        assertTrue(loc.isVerified)

        // Selecting manual state disables demo mode automatically
        viewModel.selectState("KA", "Karnataka")
        val locManual = viewModel.currentLocation.value
        assertEquals(LocationSource.MANUAL, locManual.source)
        assertFalse(locManual.isDemo)
        assertEquals("Karnataka", locManual.stateName)
        assertNull(locManual.districtName)
    }

    @Test
    fun testStaleLocationRevisionProtection() {
        val rev1 = viewModel.currentLocation.value.locationRevision
        viewModel.selectState("TG", "Telangana")
        val rev2 = viewModel.currentLocation.value.locationRevision

        assertTrue(rev2 > rev1)

        viewModel.selectState("MH", "Maharashtra")
        val rev3 = viewModel.currentLocation.value.locationRevision
        assertTrue(rev3 > rev2)

        assertEquals("Maharashtra", viewModel.currentLocation.value.stateName)
    }

    @Test
    fun testLocationSnapshot() {
        viewModel.selectState("TG", "Telangana")
        viewModel.selectDistrict("TG_RR", "Rangareddy")
        viewModel.selectMandal("TG_RR_SH", "Shamshabad", 17.2543, 78.4356, "Shamshabad Airport Area")
        viewModel.updatePincode("501218")
        viewModel.confirmLocation()

        val snapshot = viewModel.getLocationSnapshot()
        assertEquals(LocationSource.MANUAL, snapshot.source)
        assertEquals("Telangana", snapshot.stateName)
        assertEquals("Rangareddy", snapshot.districtName)
        assertEquals("Shamshabad", snapshot.mandalName)
        assertEquals("501218", snapshot.pincode)
        assertTrue(snapshot.isVerified)

        // Modifying viewmodel location does not alter snapshot
        viewModel.selectState("KA", "Karnataka")
        assertEquals("Telangana", snapshot.stateName)
    }
}
