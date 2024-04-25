package com.sujanix.cruxmdm.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.data.model.socket.location.Location
import com.sujanix.cruxmdm.data.model.socket.location.LocationTrackingDetails
import com.sujanix.cruxmdm.data.repository.CruxRepository
import com.sujanix.cruxmdm.notification.NotificationHelper
import com.sujanix.cruxmdm.util.Constant
import com.sujanix.cruxmdm.util.Constant.LOCATION_TRACKING
import com.sujanix.cruxmdm.util.UserPreferences
import com.sujanix.cruxmdm.util.location.LocationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {
    private var locationManager: LocationManager? = null
    var updateViaGps = false
    var started = false

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var repository: CruxRepository
    var count = 0
    var isInsideBoundary = false

    override fun onCreate() {
        super.onCreate()
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        repository.startSession()
    }

    private fun startAsForeground() {
        val builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                NotificationCompat.Builder(this, CHANNEL_ID)
            } else {
                NotificationCompat.Builder(this)
            }
        val notification: Notification = builder
            .setContentTitle(getString(R.string.app_name))
            .setTicker(getString(R.string.app_name))
            .setContentText("Your location is monitoring")
            .setSmallIcon(R.drawable.logo)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun requestLocationUpdates(): Boolean {
        if (updateViaGps && (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || !locationManager!!.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            ))
        ) {
            updateViaGps = false
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // No permission, so give up!
            Log.d("WEBSOCKET", "requestLocationUpdates: no permission")
            return false
        }
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val passiveEnabled = locationManager!!.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            Log.d("WEBSOCKET", "requestLocationUpdates: no internet")
            return false
        }

        try {

            val locationRequest = LocationRequest.create().apply {
                interval = Constant.LOCATION_INTERVAL
                fastestInterval = Constant.LOCATION_FASTEST_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                setSmallestDisplacement(10f)
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    val lastLatitude = result.locations.last().latitude
                    val lastLongitude = result.locations.last().longitude

                    getLocationName(26.5645, 85.9914)

                    val currentLocation = LatLng(lastLongitude, lastLatitude)
                    val fence = listOf(
                        LatLng(77.64347588986351,
                            12.959851922985962),
                        LatLng(77.64611596845509,
                            12.959674486488922),
                        LatLng(77.64561526389474,
                            12.958033192890625),
                        LatLng(77.64247448074298,
                            12.958402854554734),
                        LatLng(77.64218619629958,
                            12.960251154644183),
                        LatLng(77.64347588986351,
                            12.959851922985962)
                    )
                    val isInside = LocationUtils.isPointInsidePolygon(currentLocation, fence)

                    Log.d("CustomFence", "locationListener: $isInside, ${result.locations.last().speedAccuracyMetersPerSecond}, " +
                            "${result.locations.last().bearing}")
//                    Log.d("CustomFence", "locationListener: ${location.longitude} ${location.latitude}")
                    // Check if the location has crossed the boundary
                    if (isInside != isInsideBoundary) {
                        if (isInside) {
                            // Entered the marked area
//                            handleEnterBoundary()
                            Log.d("CustomFence", "onLocationResult: Entered the premises")
                        } else {
                            // Exited the marked area
//                            handleExitBoundary()
                            Log.d("CustomFence", "onLocationResult: Exited the premises")
                        }
                        isInsideBoundary = isInside
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val organizationData = userPreferences.accessOrganizationData.first()

                        val deviceId = userPreferences.accessDeviceId.first() ?: "3424472b6e69fe9f"

                        val locationDetail = userPreferences.accessLocationTrackingData.first()

                        if (organizationData != null && locationDetail != null) {
                            val location = LocationTrackingDetails(
                                organizationData.org_id,
                                organizationData.enterprise_id,
                                deviceId,
                                location = Location(
                                    lastLatitude,
                                    lastLongitude
                                ),
                                timeStamp = System.currentTimeMillis(),
                                locationDetail.history_enabled
                            )
                            if (locationDetail.enabled)
                                repository.sendMessageToSocket(LOCATION_TRACKING, Gson().toJson(location))
                        } else {
                            Log.d("WEBSOCKET", "onLocationResultSERVIEC: no org data present")
                            repository.startSession()
                        }
                    }

                    NotificationHelper.showNotification(
                        this@LocationService,
                        "Location monitoring...",
                        "Location \nLat: $lastLatitude Lon: $lastLongitude",
                        101
                    )
                }
            }

            LocationUtils.fUsedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            LocationUtils.fUsedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // Provider may not exist, so process it friendly
            Log.d("FATAL", "requestLocationUpdates: ${e.message}")
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun getLocationName(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this@LocationService, Locale.getDefault())
            val addressList = geocoder.getFromLocation(latitude, longitude, 2)

            if (addressList != null) {
                NotificationHelper.showNotification(
                    this@LocationService,
                    "Location",
                    "Location: ${addressList.last().getAddressLine(0)}",
                    102
                )
//                Log.d("FATAL", "getLocationName: ${addressList.last().getAddressLine(0)}}")
                Log.d("FATAL", "getLocationName: ${addressList.first().getAddressLine(0)}")
            }
        } catch (e: Exception){
            e.printStackTrace()
            Log.d("FATAL", "getLocationName: ${e.message}")
        }

    }

    override fun onDestroy() {
        started = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(inputIntent: Intent, flags: Int, startId: Int): Int {
//        Log.d("WEBSOCKET", "onStartCommand: $count triggered")
        val legacyGpsFlag = updateViaGps
        if (inputIntent.action != null) {
            if (inputIntent.action == ACTION_STOP) {
                // Stop service
                started = false
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            } else updateViaGps = inputIntent.action == ACTION_UPDATE_GPS
        } else {
            updateViaGps = false
        }
//        socketClientImp.startSession()
        if (!started || legacyGpsFlag != updateViaGps) {
            if (!requestLocationUpdates()) {
                // No permissions!
                started = false
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        if (!started) {
            startAsForeground()
            started = true
        }

        if(inputIntent.action == LOCATION_TRACKING)
            Log.d("WEBSOCKET", "onStartCommand: $count triggered")

        CoroutineScope(Dispatchers.IO).launch {
            repository.sendBatteryDataPeriodically()
        }

        return START_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 112
        var CHANNEL_ID = LocationService::class.java.name
        const val ACTION_UPDATE_GPS = "gps"
        const val ACTION_UPDATE_NETWORK = "network"
        const val ACTION_STOP = "stop"
        private const val LOCATION_UPDATE_INTERVAL = 60000
    }
}