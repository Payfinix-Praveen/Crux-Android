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
import com.google.gson.Gson
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.LocationEntity
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates
import com.sujanix.cruxmdm.features.core.data.model.fencing.fence_log.CurrentLocation
import com.sujanix.cruxmdm.features.core.data.model.fencing.fence_log.FenceLog
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting
import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.NotificationHelper
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Constant.LOCATION_TRACKING
import com.sujanix.cruxmdm.features.core.utlis.Constant.PREFERENCE_NAME
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.location.LocationUtils
import com.sujanix.cruxmdm.socket.model.LiveTrackingData
import com.sujanix.cruxmdm.socket.model.TrackingData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {
    private var currentLocationName: String = ""
    private var priority: Int = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    private var uemSettings: UemSetting? = null
    private var breadcrumbSettings: Boolean? = null
    private var previousLocationName: String = ""
    private val TAG: String = "LocationService"
    private var locationManager: LocationManager? = null
    private var updateViaGps = false
    var started = false

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var repository: CruxRepository
    private var isSocketConnected: Boolean = false
    var count = 0
    var isInsideBoundary = false

    override fun onCreate() {
        super.onCreate()
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

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
//
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
            Log.d(TAG, "requestLocationUpdates: no permission")
            return false
        }
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val passiveEnabled = locationManager!!.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            Log.d(TAG, "requestLocationUpdates: no internet")
            return false
        }

        try {
            val preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
            val locationTrackingEnabled = preferences.getBoolean(LOCATION_TRACKING, false)
            Log.d(TAG, "locationTrackingEnabled: $locationTrackingEnabled")

            priority = if(locationTrackingEnabled){
                LocationRequest.PRIORITY_HIGH_ACCURACY
            } else {
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }

            CoroutineScope(Dispatchers.IO).launch {
                uemSettings = repository.getUemSettings().first()
            }
//            val miniDisplacement = uemSettings?.range?.toFloat() ?: 50F
            val miniDisplacement = if(locationTrackingEnabled) 5f else 50f
            val locationRequest = LocationRequest
                .Builder(priority, Constant.LOCATION_INTERVAL)
                .setMinUpdateIntervalMillis(Constant.LOCATION_FASTEST_INTERVAL)     // TODO: Update interval in milliseconds from the server
                .setMinUpdateDistanceMeters(miniDisplacement)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    val lastLatitude = result.locations.last().latitude
                    val lastLongitude = result.locations.last().longitude

                    currentLocationName = getLocationName(lastLatitude,lastLongitude)

                    CoroutineScope(Dispatchers.IO).launch {

//                        val deviceId = "3f37439c1c19643e"
                        val speed = result.locations.last().speedAccuracyMetersPerSecond
                        val bearing = result.locations.last().bearing
                        val direction = LocationUtils.getDirectionFromBearing(bearing)

                        userPreferences.accessDeviceUserData.collect { deviceUserData ->
                            val deviceId = userPreferences.accessDeviceId.first() ?: "3f37439c1c19643e"
                            checkFenceStatus(lastLatitude, lastLongitude, deviceId, deviceUserData)
                            Log.d(TAG, "deviceUserData: $deviceUserData")
                            if (deviceUserData != null) {
                                Log.d(TAG, "onLocationResult: called1")
//                                val locationDetail = userPreferences.accessLocationTrackingData.last()
                                saveLocation(
                                    Constant.LOCATION_HISTORY,
                                    currentLocationName,
                                    lastLatitude,
                                    lastLongitude
                                )
                                checkSettingAndSaveBreadcrumb(currentLocationName, lastLatitude, lastLongitude)

                                if (locationTrackingEnabled) {

                                    /** LIVE LOCATION TRACKING DATA */
                                    val trackingData = TrackingData(
                                        device_id = deviceId,
                                        direction = direction,
                                        enterprise_id = deviceUserData.enterprise_id,
                                        geo_coding = currentLocationName,
                                        latitude = lastLatitude.toString(),
                                        longitude = lastLongitude.toString(),
                                        speed = speed.toString(),
                                        timestamp = System.currentTimeMillis().toString(),
                                    )

                                    repository.sendMessageToSocket(
                                        message = Gson().toJson(
                                            LiveTrackingData(
                                                action = "onMessage",
                                                message = trackingData
                                            )
                                        ).toString()
                                    )
                                }
                            }
                        }
                    }

//                    NotificationHelper.showNotification(
//                        this@LocationService,
//                        "Location monitoring...",
//                        "Location \nLat: $lastLatitude Lon: $lastLongitude",
//                        101
//                    )
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
            Log.d(TAG, "requestLocationUpdates: ${e.message}")
            e.printStackTrace()
            return false
        }
        return true
    }

    private suspend fun checkSettingAndSaveBreadcrumb(
        currentLocationName: String,
        lastLatitude: Double,
        lastLongitude: Double
    ) {
        val breadcrumbSettings = repository.getBreadcrumbSettings().first()
        if (breadcrumbSettings == true && previousLocationName != currentLocationName) {
            saveLocation(
                Constant.LOCATION_BREADCRUMBS,
                currentLocationName,
                lastLatitude,
                lastLongitude
            )
            previousLocationName = currentLocationName
        }
    }

    private suspend fun checkFenceStatus(
        lastLatitude: Double,
        lastLongitude: Double,
        deviceId: String,
        deviceUserData: DeviceUserData?
    ) {
        val fenceLogs = FenceLog(
            currentLocation = CurrentLocation(lastLongitude, lastLongitude, currentLocationName),
            deviceId = deviceId,
            email = deviceUserData?.email ?: "",
//            email = "abcdefghijklmnopqrstuvwxyz@gmail.com",
            enterpriseId = deviceUserData?.enterprise_id ?: "",
            fenceId = -1
        )
        val fenceData = repository.getAllFenceData()
        if (fenceData.isNotEmpty()) {

            for (fence in fenceData) {
//                if (fence.fenceType == "MultiPolygon") {
                    when (fence.coordinates) {
                        is Coordinates.Polygon -> {
                            val isInside =
                                LocationUtils.isPointInPolygon(
                                    fence.coordinates.coordinates,
                                    lastLatitude,
                                    lastLongitude
                                )
                            if (isInside != isInsideBoundary) {
                                if (isInside) {
                                    Log.d(TAG, "CUSTOMFENCE: Entered the premises")
                                    val fenceLog = fenceLogs.copy(
                                        alerts = listOf(),
                                        fenceId = fence.fenceId.toInt(),
                                        insideFence = true
                                    )
                                    repository.logFence(fenceLog)
                                    isInsideBoundary = true
                                    break
                                } else {
                                    Log.d(TAG, "CUSTOMFENCE: Exited the premises")
                                }
                                isInsideBoundary = isInside
                            }
                        }
                        else -> {}
                    }
//                }
            }
            if (!isInsideBoundary) {
                Log.d(TAG, "checkFenceStatus: Outside fences")
                val fenceLog = fenceLogs.copy(
                    fenceId = -1,
                    insideFence = false
                )
                repository.logFence(fenceLog)
            }
        }
    }

    private suspend fun saveLocation(
        type: String,
        locationName: String,
        latitude: Double,
        longitude: Double
    ) {
        repository.insertLocationData(
            LocationEntity(
                type = type,
                geoCoding = LocationUtils.extractAreaFromLocation(locationName),
                lat = latitude.toString(),
                lon = longitude.toString(),
                timestamp = System.currentTimeMillis().toString()
            )
        )
    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(this@LocationService, Locale.getDefault())
            val addressList = geocoder.getFromLocation(latitude, longitude, 2)

            if (addressList != null) {
                NotificationHelper.showNotification(
                    this@LocationService,
                    "Location",
                    "Location: ${addressList.last().getAddressLine(0)}",
                    102
                )
                
                Log.d(TAG, "getLocationName: ${addressList.first().getAddressLine(0)}")
                addressList.first().getAddressLine(0).toString()
            } else ""
        } catch (e: Exception){
            e.printStackTrace()
            Log.d(TAG, "getLocationName: ${e.message}")
            ""
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
            if(inputIntent.action == ACTION_START_LIVE_TRACKING){
                repository.startSession()
            }
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
            Log.d(TAG, "onStartCommand: $count triggered")

        CoroutineScope(Dispatchers.IO).launch {
            repository.sendBatteryDataPeriodically()
        }

        return START_STICKY
    }

    companion object {
        val ACTION_START_LIVE_TRACKING: String = "start_live_tracking"
        private const val NOTIFICATION_ID = 112
        var CHANNEL_ID = LocationService::class.java.name
        const val ACTION_UPDATE_GPS = "gps"
        const val ACTION_UPDATE_NETWORK = "network"
        const val ACTION_STOP = "stop"
        private const val LOCATION_UPDATE_INTERVAL = 60000
    }
}