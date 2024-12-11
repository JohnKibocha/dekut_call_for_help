// app/src/main/java/com/muriithi/dekutcallforhelp/components/LocationManager.kt
package com.muriithi.dekutcallforhelp.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationManager(private val context: Context, private val userId: String) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .build()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                uploadLocation(location)
            }
        }
    }
    private val firebaseService = FirebaseService()

    @SuppressLint("MissingPermission")
    fun startTracking() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun uploadLocation(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            firebaseService.getUserById(userId) { user ->
                user?.let {
                    it.latitude = location.latitude
                    it.longitude = location.longitude
                    firebaseService.updateUser(it) { success ->
                        if (success) {
                            // Log success
                        } else {
                            Log.e("LocationManager", "Failed to upload location")
                        }
                    }
                }
            }
        }
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}