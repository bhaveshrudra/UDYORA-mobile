package com.example.services

import kotlin.math.*

object GeoDistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two geographic points using the Haversine formula.
     * Returns distance in meters.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2.0).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2.0).pow(2.0)

        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates distance in kilometers rounded to two decimal places.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val meters = calculateDistanceMeters(lat1, lng1, lat2, lng2)
        return (meters / 1000.0 * 100.0).roundToInt() / 100.0
    }
}
