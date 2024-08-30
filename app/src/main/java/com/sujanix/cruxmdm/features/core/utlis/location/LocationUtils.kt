package com.sujanix.cruxmdm.features.core.utlis.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.core.data.model.request.LocationDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.Property
import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate
import com.sujanix.cruxmdm.features.core.presentation.view.MainActivity
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.showDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocationUtils {
    lateinit var fUsedLocationClient: FusedLocationProviderClient

    interface LocationListener {
        fun onLocationReceived(latitude_: String, longitude_: String)
        fun onLocationError()
    }

    fun getLocation(context: Context, listener: LocationListener) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                123
            )
            listener.onLocationError()
        } else {

//            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            fUsedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fUsedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    listener.onLocationReceived (
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                } else {
                    listener.onLocationError()
                }
            }
        }
    }

    fun checkLocationPermissions(activity: Activity, context: Context): Boolean {
        return if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity.checkSelfPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            var activeModeLocation = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    activeModeLocation =
                        activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                                && !ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                } catch (e: java.lang.Exception) {
                    // On some older models:
                    // java.lang.IllegalArgumentException
                    // Unknown permission: android.permission.ACCESS_BACKGROUND_LOCATION
                    // Update: since there's the Android version check, we should never be here!
                    e.printStackTrace()
                    Log.d("FATAL", "checkLocationPermissions1: ${e.message}")
                }
            }
            if (activeModeLocation) {
                // The following flow happened
                // The user has enabled locations, but when the app prompted for the background location,
                // the user clicked "Locations only in active mode".
                // In this case, requestPermissions won't show dialog any more!
                // So we need to open the general permissions dialog
                // Let's confirm with the user once again, then display the settings sheet
                try {
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent("ENABLE_SETTINGS"))
                    val message = context.getString (
                        R.string.background_location,
                        context.getString(R.string.app_name)
                    )
                    showDialog (
                        context = context,
                        "Permission Required",
                        message,
                        positiveBtnText = context.getString(R.string.background_location_continue),
                        negativeBtnText = context.getString(R.string.location_disable),
                        positiveBtnListener = {
                            activity.startActivity(
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        }
                    )
                } catch (e: java.lang.Exception) {
                    // Activity closed before showing a dialog, just ignore this exception
                    e.printStackTrace()
                    Log.d("FATAL", "checkLocationPermissions2: ${e.message}")
                }
            } else {
                activity.requestPermissions(
                    MainActivity.REQUIRED_PERMISSIONS,
                    MainActivity.REQUEST_CODE_PERMISSIONS
                )
            }
            false
        } else {
            true
        }
    }

    fun isPointInsidePolygon(point: LatLng, polygonCorners: List<LatLng>): Boolean {

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

    fun isPointInPolygon(multipolygon: List<List<Coordinate>>, pointX: Double, pointY: Double): Boolean {
        for (poly in multipolygon) {
            var i = 0
            var j = poly.lastIndex
            var inside = false
            while (i < poly.size) {
                val condition1 = (poly[i].longitude > pointY) != (poly[j].longitude > pointY)
                val condition2 = pointX < (poly[j].latitude - poly[i].latitude) * (pointY - poly[i].longitude) / (poly[j].longitude - poly[i].longitude) + poly[i].latitude
                if (condition1 && condition2) {
                    inside = !inside
                }
                j = i
                i++
            }
            if (inside) {
                return true
            }
        }
        return false
    }

    private fun isPointInPolygon(tap: LatLng, vertices: List<LatLng>): Boolean {
        var intersectCount = 0
        for (j in 0 until vertices.size - 1) {
            if (rayCastIntersect(tap, vertices[j], vertices[j + 1])) {
                intersectCount++
            }
        }

        return ((intersectCount % 2) == 1) // odd = inside, even = outside;
    }

    private fun rayCastIntersect(tap: LatLng, vertA: LatLng, vertB: LatLng): Boolean {
        return try {
            val aY = vertA.latitude
            val bY = vertB.latitude
            val aX = vertA.longitude
            val bX = vertB.longitude
            val pY = tap.latitude
            val pX = tap.longitude

            if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
                return false // a and b can't both be above or below pt.y, and a or
                // b must be east of pt.x
            }

            val m = (aY - bY) / (aX - bX) // Rise over run
            val bee = (-aX) * m + aY // y = mx + b
            val x = (pY - bee) / m // algebra is neat!

            x > pX

        } catch (e:Exception){
            false
        }
    }

    fun getDirectionFromBearing(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val sectors = arrayOf(45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f)

        val index = sectors.indexOfFirst { bearing <= it } % sectors.size
        return directions[index]
    }

    fun extractAreaFromLocation(location: String): String {
        val words = location.trim().split(" ")
        return if (words.size < 5) {
            location // Return the entire location if less than 4 words
        } else {
            words.subList(words.size - 5, words.size).joinToString(" ") // Join the last 4 words
        }
    }

    fun groupLocationsByDate(locations: List<Property>): List<Pair<String, List<Property>>> {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        return locations.groupBy { location ->
            val date = Date(location.timestamp.toLong())
            dateFormat.format(date).toLong()
        }.map { (date, properties) ->
            Pair(date.toString(), properties)
        }
    }

    fun organizeAndSyncLocationData(
        gpsBreadcrumbs:  List<Pair<String, List<Property>>>,
        userData: DeviceUserData?,
        deviceId: String,
        type: String,
        syncData: (List<LocationDataRequest>, String) -> Unit
    ) {

        val trackingType = if(type == Constant.LOCATION_BREADCRUMBS) Constant.LOCATION_BREADCRUMBS else Constant.LOCATION_HISTORY
        val locationDataByDate = gpsBreadcrumbs.map { locationData ->
            val lastLocationTimeStamp = locationData.second.last().timestamp
            CoroutineScope(Dispatchers.IO).launch {
            }
            LocationDataRequest(
//                deviceId = userData!!.device_user_id,
                deviceId = "3f37439c1c19643e",
                enterpriseId = userData!!.enterprise_id,
                properties = locationData.second,
                trackingType = trackingType,
                userId = userData.device_user_id,
                appSyncTime = (lastLocationTimeStamp.toLong() / 1000).toString()
            )
        }
        syncData(locationDataByDate, trackingType)
    }
}