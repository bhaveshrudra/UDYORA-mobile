package com.example.services

/**
 * Explicit encapsulation of Demo Location Scenarios.
 * Used ONLY when user explicitly chooses "Demo Scenario".
 * Real user assessments must NEVER silently fallback to demo values.
 */
object DemoScenario {
    const val STATE_ID = "TG"
    const val STATE_NAME = "Telangana"
    const val DISTRICT_ID = "TG_RR"
    const val DISTRICT_NAME = "Rangareddy"
    const val MANDAL_ID = "TG_RR_SH"
    const val MANDAL_NAME = "Shamshabad"
    const val PINCODE = "501218"
    const val LOCALITY = "Shamshabad Airport Area (Demo Scenario)"
    const val LATITUDE = 17.2543
    const val LONGITUDE = 78.4356
}
