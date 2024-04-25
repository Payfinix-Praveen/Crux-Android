package com.sujanix.cruxmdm.util.location

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.sujanix.cruxmdm.notification.NotificationHelper


/**
 * CustomGeofence class provides functionality to monitor a user's location
 * and trigger custom functions when the user enters or exits a marked area.
 * It implements a custom geofencing mechanism without using the Geofencing API
 * by continuously tracking the user's location and determining if it is inside
 * or outside a predefined boundary defined by a polygon.
 *
 * The process involves:
 * 1. Defining the boundary coordinates of the marked area.
 * 2. Monitoring the user's location using location updates.
 * 3. Determining if the user's location is inside or outside the marked area
 *    using a point-in-polygon algorithm.
 * 4. Triggering custom functions when the user's location crosses the boundary
 *    of the marked area.
 *
 * @param context The context used for location services.
 * @param boundaryCoordinates The list of LatLng coordinates defining the boundary of the marked area.
 */
class CustomGeofence(
    private val context: Context,
    private val boundaryCoordinates: List<LatLng>
) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isInsideBoundary = false

    fun startTracking() {
        // Request location updates
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val locationListener: LocationListener = LocationListener { location ->
//        val currentLocation = LatLng(location.longitude, location.latitude)
        val currentLocation = LatLng(77.647172, 12.958964)
        val isInside = isPointInsidePolygon(currentLocation, boundaryCoordinates)

        Log.d("CustomFence", "locationListener: $isInside")
        Log.d("CustomFence", "locationListener: ${location.longitude} ${location.latitude}")
        // Check if the location has crossed the boundary
        if (isInside != isInsideBoundary) {
            if (isInside) {
                // Entered the marked area
                handleEnterBoundary()
            } else {
                // Exited the marked area
                handleExitBoundary()
            }
            isInsideBoundary = isInside
        }
    }

    private fun isPointInsidePolygon(point: LatLng, polygonCorners: List<LatLng>): Boolean {

        Log.d("CustomFence", "isPointInsidePolygon: point: $point \n corners: $polygonCorners")
        var isInside = false
        var j = polygonCorners.size - 1
        for (i in polygonCorners.indices) {
            val vertex1 = polygonCorners[i]
            val vertex2 = polygonCorners[j]
            if (vertex1.longitude > point.longitude && vertex2.longitude >= point.longitude ||
                vertex2.longitude > point.longitude && vertex1.longitude >= point.longitude
            ) {
                Log.d("CustomFence", "isPointInsidePolygon: ${vertex1.latitude + (point.longitude - vertex1.longitude)} / ${(vertex2.longitude - vertex1.longitude) * (vertex2.latitude - vertex1.latitude)} < ${point.latitude}")
                Log.d("CustomFence", "sdfsg: ${vertex1.latitude + (point.longitude - vertex1.longitude) / (vertex2.longitude - vertex1.longitude) * (vertex2.latitude - vertex1.latitude)} < ${point.latitude}")
                if (vertex1.latitude + (point.longitude - vertex1.longitude) / (vertex2.longitude - vertex1.longitude) * (vertex2.latitude - vertex1.latitude) < point.latitude) {
                    isInside = !isInside
                }
            }
            j = i
        }
        return isInside
    }

    private fun handleEnterBoundary() {
        NotificationHelper.showNotification(
            context,
            "Crux-MDM",
            "You've entered the premises.",
            1001
        )
    }

    private fun handleExitBoundary() {
        NotificationHelper.showNotification(
            context,
            "Crux-MDM",
            "You've exited the premises.",
            1001
        )
    }

    companion object {
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1 // 1 minute
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 1f // 1 meters
    }
}