package com.sujanix.cruxmdm.service

import android.util.Log
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.sujanix.cruxmdm.commands.CommandUtils
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.selfHosted.Data
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.features.auth.data.repository.AuthRepository
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSON
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates
import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.fencing.GeoJson
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.location.LocationUtils
import com.sujanix.cruxmdm.features.core.utlis.broadcastSettings
import com.sujanix.cruxmdm.worker.ApkDownloadWorker
import com.sujanix.cruxmdm.worker.ServiceWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class CruxFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var cruxRepository: CruxRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var appStoreRepository: AppCatalogRepository

    private val gson = Gson()
    val TAG = "CruxFirebaseInstanceIdService"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + message.data)

            val type = message.data["type"]
            val status = message.data["status"]
            val data = message.data["data"]

            CoroutineScope(Dispatchers.IO).launch {
                when (type) {
                    Constant.LOCATION_MANAGEMENT -> {
                        updateLocationConfig(data)
                    }

                    Constant.CONTENT_MANAGEMENT -> {

                    }

                    Constant.APP_MANAGEMENT -> {
                        updateAppStore(data)
                    }

                    Constant.REMOTE_CONTROL -> {

                    }

                    Constant.ADMIN_ACTIVITY -> {

                    }
                }
            }
        }
    }

    private fun updateAppStore(data: String?) {

        if (data == null) return

        val appData = JSONObject(data)
        val type = appData.getString("type")
        val appDetailData = appData.getJSONObject("data")
        val appDetail = appDetailData.getJSONObject("app_details")

        when (type) {
            "ADD" -> { addApp(appDetail) }
            "UPDATE" -> { checkAndUpdateApp(appDetail) }
            "DELETE" -> { /* TODO: delete app from app store */ }
        }
    }

    private fun addApp(appDetail: JSONObject) {
        val appData = gson.fromJson(appDetail.toString(), Data::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "addApp: ${appData.toApplication()}")
            appStoreRepository.insertApplication(appData.toApplication())
        }

        Log.d(TAG, "addApp: ${appData.type} ${Constant.SELF_HOSTED_APP} = ${appData.type == Constant.SELF_HOSTED_APP}")
        Log.d(TAG, "addApp: ${appData.app_distribution_method} ${Constant.SILENT_INSTALL} = ${appData.app_distribution_method == Constant.SILENT_INSTALL}")
        if(appData.type == Constant.SELF_HOSTED_APP && appData.app_distribution_method == Constant.SILENT_INSTALL){

            Log.d(TAG, "addApp: ${Gson().toJson(appData.toApplication())}")
            val packageName = appDetail.getString("bundle_identifier")
            val apkUrl = appDetail.getString("signed_url")
            val data = workDataOf(
                "type" to appDetail.getString("type"),
                "name" to appDetail.getString("name"),
                "pkg" to packageName,
                "url" to apkUrl,
                "version" to appDetail.getString("version"),
                "versionCode" to appDetail.getInt("version_code"),
                "iconUrl" to appDetail.getString("app_logo_base64")
            )

            ApkDownloadWorker.downloadApk(this@CruxFirebaseMessagingService, data)
        }
    }

    private fun checkAndUpdateApp(appDetail: JSONObject) {

//        TODO: check if app is present in app store if not add it.

        CoroutineScope(Dispatchers.IO).launch {
            val packageName = appDetail.getString("bundle_identifier")
            val apkUrl = appDetail.getString("signed_url")
            val app = Application(
                type = appDetail.getString("type"),
                name = appDetail.getString("name"),
                pkg = packageName,
                url = apkUrl,
                version = appDetail.getString("version"),
                versionCode = appDetail.getInt("version_code"),
                iconUrl = appDetail.getString("app_logo_base64")
            )
            appStoreRepository.updateApplication(
                app,
                pkgName = packageName, isUpdateAvailable = true
            )

            val data = workDataOf(
                "type" to appDetail.getString("type"),
                "name" to appDetail.getString("name"),
                "pkg" to packageName,
                "url" to apkUrl,
                "version" to appDetail.getString("version"),
                "versionCode" to appDetail.getInt("version_code"),
                "iconUrl" to appDetail.getString("app_logo_base64")
            )

            ApkDownloadWorker.downloadApk(this@CruxFirebaseMessagingService, data)
        }
    }

    private suspend fun updateLocationConfig(data: String?) {
        Log.d(TAG, "updateLocationConfiguration: $data")

        if(data == null) return

        val settings = JSONObject(data)
        val type = settings.getString("type")
        val settingsData = settings.getJSONObject("data")

        when(type) {
            Constant.LOCATION_HISTORY -> {
                updateLocationHistoryConfig(data)
            }

            Constant.LOCATION_BREADCRUMBS -> {
                updateLocationBreadcrumbsConfig(settingsData)
            }

            Constant.LIVE_TRACKING -> {
                updateLiveTrackingConfig(settingsData)
            }

            Constant.GEO_FENCE -> {
                updateGeofenceConfig(settingsData)
            }

            Constant.SYNC_DATA -> {
                syncLocationData()
            }

            Constant.CLEAR_APP_DATA -> {
                CommandUtils.issueClearAppDataCommand(this, listOf(settingsData.getString("pkg")))
            }
        }
    }

    private suspend fun updateGeofenceConfig(settingsData: JSONObject) {
        val enable = settingsData.getString("status")
        if (enable == "ENABLED") {
            //TODO: make api call to fetch geo fence data
            Log.d(TAG, "updateLocationConfig: $settingsData")
            val fenceId = settingsData.getString("fence_id")
            val type = settingsData.getString("type")
            when(type) {
                "ADD" -> {
                    addFence(fenceId)
                }
                "UPDATE" -> {
//                    updateFence(fenceId)
                }
            }
        }
    }

    private suspend fun addFence(fenceId: String){
        cruxRepository.getDeviceId().collect { deviceId ->
            cruxRepository.getUserData().collect { userData ->
                if(deviceId != null && userData != null) {
                    val fenceData =
                        cruxRepository.getFenceData(userData.enterprise_id, deviceId, fenceId)
                    manageFence(fenceData)
                }
            }
        }
    }

    private suspend fun manageFence(fenceData: Fencing){
        val boundaryIdList = fenceData.boundary_id
        if (boundaryIdList.isNotEmpty()){
            for (boundaryId in boundaryIdList) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = async{ cruxRepository.getBoundaryCoordinates(boundaryId) }
                    getFenceData(boundaryId, fenceData, response.await())
                }
            }
        } else {
            cruxRepository.insertFenceData(
                GeoJSON(
                    fenceId = fenceData.fenceID.toString(),
                    fenceType = fenceData.fenceType,
                    boundaryId = "N/A",
                    coordinates = Coordinates.Polygon(fenceData.geoJson.coordinates),
                    coordinateType = fenceData.geoJson.coordinatesType
                )
            )
        }
    }

    private fun getFenceData(boundaryId: String, fenceData: Fencing, response: GeoJson){
        CoroutineScope(Dispatchers.IO).launch {
            val coordinatesType = response.coordinatesType
            cruxRepository.insertFenceData(
                GeoJSON(
                    fenceId = fenceData.fenceID.toString(),
                    fenceType = fenceData.fenceType,
                    boundaryId = boundaryId,
                    coordinates = Coordinates.Polygon(response.coordinates),
                    coordinateType = coordinatesType
                )
            )
        }
    }

    private fun updateLiveTrackingConfig(settingsData: JSONObject) {
        val isEnable = settingsData.getString("status") == "ENABLED"
        val preferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(Constant.LOCATION_TRACKING, isEnable)
        editor.putString(Constant.ROOM_ID, settingsData.getString("roomId"))
        editor.apply()

        ServiceWorker.startLocationService(this)
    }

    private suspend fun updateLocationBreadcrumbsConfig(settingsData: JSONObject) {
        val isEnabled = settingsData.getString("breadcrumbs") == "ENABLED"
        cruxRepository.setBreadcrumbSettings(isEnabled)
        val preferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(Constant.LOCATION_BREADCRUMBS, isEnabled)
        editor.apply()
        ServiceWorker.startLocationService(this)
    }

    private suspend fun updateLocationHistoryConfig(data: String?) {
        cruxRepository.setUemSettings(gson.fromJson(data, UemSetting::class.java))
        ServiceWorker.startLocationService(this)
    }

    private fun syncLocationData() {

        try {
            CoroutineScope(Dispatchers.IO).launch {

                Log.d(TAG, "LocationManagementWorker: Called")

                val locationHistory = cruxRepository.getLocationDataByType(Constant.LOCATION_HISTORY)
                val locationHistoryByDate = LocationUtils.groupLocationsByDate(locationHistory)
                val gpsBreadcrumbs = cruxRepository.getLocationDataByType(Constant.LOCATION_BREADCRUMBS)
                val gpsBreadcrumbsByDate = LocationUtils.groupLocationsByDate(gpsBreadcrumbs)

                Log.d(TAG, "LocationManagementWorker: locationHistory $locationHistory")
                Log.d(TAG, "LocationManagementWorker: gpsBreadcrumbs $gpsBreadcrumbs")
                cruxRepository.getUserData().collect{ deviceUserData ->
                    cruxRepository.getDeviceId().collect { deviceId ->
                        if(deviceId != null) {
                            Log.d(TAG, "LocationManagementWorker: deviceUserData $deviceUserData")
                            if(deviceUserData != null) {
                                if (locationHistoryByDate.isNotEmpty()) {
                                    LocationUtils.organizeAndSyncLocationData(
                                        locationHistoryByDate,
                                        deviceUserData,
                                        deviceId,
                                        Constant.LOCATION_HISTORY
                                    ){ locationData, trackingType ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = cruxRepository.sendLocationData(
                                                locationData,
                                                trackingType
                                            )
                                        }
                                    }

                                    Log.d(TAG, "LocationManagementWorker: locationHistory list is not empty")
                                }
                                if (gpsBreadcrumbsByDate.isNotEmpty()) {
                                    LocationUtils.organizeAndSyncLocationData(
                                        gpsBreadcrumbsByDate,
                                        deviceUserData,
                                        deviceId,
                                        Constant.LOCATION_BREADCRUMBS
                                    ){ locationData, trackingType ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = cruxRepository.sendLocationData(
                                                locationData,
                                                trackingType
                                            )
                                        }
                                    }
                                    Log.d(TAG, "LocationManagementWorker: locationBreadcrumb list is not empty")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")

        CoroutineScope(Dispatchers.IO).launch {
            cruxRepository.getUserData().collect { userData ->
                if(userData != null) {
                    authRepository.loginUser(
                        LoginData(
                            userData.email,
                            token
                        )
                    )
                }
            }
        }
    }
}