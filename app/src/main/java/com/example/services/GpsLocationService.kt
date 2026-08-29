package com.example.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.types.CanonicalLocation
import com.example.types.LocationSource
import com.example.types.LocationVerificationStatus
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

object GpsLocationService {

    sealed class GpsResult {
        data class Success(val location: CanonicalLocation) : GpsResult()
        data class Error(val message: String) : GpsResult()
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentHardwareLocation(
        context: Context,
        revision: Long
    ): GpsResult = withContext(Dispatchers.IO) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancelTokenSource = CancellationTokenSource()

            val location: Location? = try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancelTokenSource.token
                ).await()
            } catch (e: Exception) {
                fusedClient.lastLocation.await()
            }

            if (location == null) {
                return@withContext GpsResult.Error("Unable to acquire GPS coordinates. Please ensure GPS is turned on.")
            }

            val lat = location.latitude
            val lng = location.longitude
            val accuracy = location.accuracy
            val time = location.time

            val geocodeResult = reverseGeocode(context, lat, lng)

            val resolvedStateName = geocodeResult?.adminArea
            val resolvedDistrictName = geocodeResult?.subAdminArea ?: geocodeResult?.locality
            val resolvedMandalName = geocodeResult?.subLocality ?: geocodeResult?.locality ?: geocodeResult?.subAdminArea
            val resolvedPincode = geocodeResult?.postalCode
            val resolvedLocality = geocodeResult?.featureName ?: geocodeResult?.subLocality ?: geocodeResult?.locality

            val matchedHierarchy = matchWithHierarchy(resolvedStateName, resolvedDistrictName, resolvedMandalName, resolvedPincode)

            val canonical = CanonicalLocation(
                source = LocationSource.GPS,
                stateId = matchedHierarchy?.first?.stateId,
                stateName = matchedHierarchy?.first?.stateName ?: resolvedStateName,
                districtId = matchedHierarchy?.second?.districtId,
                districtName = matchedHierarchy?.second?.districtName ?: resolvedDistrictName,
                mandalId = matchedHierarchy?.third?.mandalId,
                mandalName = matchedHierarchy?.third?.mandalName ?: resolvedMandalName,
                pincode = matchedHierarchy?.third?.pincodes?.firstOrNull() ?: resolvedPincode,
                locality = resolvedLocality ?: "Detected Location Area",
                latitude = lat,
                longitude = lng,
                accuracyMeters = accuracy,
                timestamp = time,
                verificationStatus = if (matchedHierarchy != null || (!resolvedStateName.isNullOrBlank() && !resolvedDistrictName.isNullOrBlank()))
                    LocationVerificationStatus.VERIFIED
                else
                    LocationVerificationStatus.PENDING,
                locationRevision = revision
            )

            GpsResult.Success(canonical)
        } catch (e: Exception) {
            GpsResult.Error("Location detection failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun reverseGeocode(context: Context, lat: Double, lng: Double): Address? {
        return try {
            if (!Geocoder.isPresent()) return null
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun matchWithHierarchy(
        stateName: String?,
        districtName: String?,
        mandalName: String?,
        pincode: String?
    ): Triple<LocationNode, DistrictNode, MandalNode>? {
        if (!pincode.isNullOrBlank()) {
            val byPin = LocationHierarchyService.findByPincode(pincode)
            if (byPin != null) return byPin
        }

        if (stateName.isNullOrBlank()) return null
        val stateNode = LocationHierarchyService.states.find {
            it.stateName.equals(stateName, ignoreCase = true) || it.stateName.contains(stateName, ignoreCase = true)
        } ?: return null

        if (districtName.isNullOrBlank()) return null
        val distNode = stateNode.districts.find {
            it.districtName.equals(districtName, ignoreCase = true) || it.districtName.contains(districtName, ignoreCase = true)
        } ?: stateNode.districts.firstOrNull() ?: return null

        val mandalNode = if (!mandalName.isNullOrBlank()) {
            distNode.mandals.find {
                it.mandalName.equals(mandalName, ignoreCase = true) || it.mandalName.contains(mandalName, ignoreCase = true)
            } ?: distNode.mandals.firstOrNull()
        } else {
            distNode.mandals.firstOrNull()
        } ?: return null

        return Triple(stateNode, distNode, mandalNode)
    }
}
