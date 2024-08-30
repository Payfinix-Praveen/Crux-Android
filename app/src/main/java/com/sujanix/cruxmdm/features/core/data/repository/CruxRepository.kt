package com.sujanix.cruxmdm.features.core.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSON
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.LocationEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.LocationEntity.Companion.toProperty
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.toGeoJSON
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.toGeoJSONEntity
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.fencing.GeoJson
import com.sujanix.cruxmdm.features.core.data.model.fencing.fence_log.FenceLog
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting
import com.sujanix.cruxmdm.features.core.data.model.request.BoundaryCoordinatesRequest
import com.sujanix.cruxmdm.features.core.data.model.request.FenceDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.LocationDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.Property
import com.sujanix.cruxmdm.features.core.data.model.request.initial_request.DeviceEnrollmentData
import com.sujanix.cruxmdm.features.core.data.model.response.LocationDataResponse
import com.sujanix.cruxmdm.features.core.data.model.socket.Configuration
import com.sujanix.cruxmdm.features.core.data.model.socket.battery.BatteryData
import com.sujanix.cruxmdm.features.core.data.model.socket.battery.BatteryTrackingDetails
import com.sujanix.cruxmdm.socket.SocketClient
import com.sujanix.cruxmdm.socket.SocketClientImp
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.battery.BatteryDataHelper
import com.sujanix.cruxmdm.features.core.utlis.hourAndMinutesToMillis
import com.sujanix.cruxmdm.features.core.utlis.runOnInterval
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar
import javax.inject.Inject

open class CruxRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val locationApi: LocationApi,
    private val appDao: ApplicationDao,
    private val locationDao: LocationDataDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
) : BaseRepository(), SocketClientImp.SocketListener {

    private val TAG: String = "CruxRepository"
    private val socketClient: SocketClient by lazy {
        SocketClientImp(context, userPreference, this)
    }

    fun getUserData() = userPreference.accessDeviceUserData

    suspend fun setDeviceEnrolled() {
        userPreference.setDeviceEnrolled()
    }

    suspend fun clearUserData() {
        userPreference.clear()
    }

    fun getDeviceId() = userPreference.accessDeviceId

    suspend fun setBreadcrumbSettings(enabled: Boolean) {
        userPreference.saveBreadcrumbData(enabled)
    }

    fun getBreadcrumbSettings() = userPreference.accessBreadcrumbData

    suspend fun setGeofenceSettings(fencing: Fencing) {
        userPreference.saveGeofenceData(fencing)
    }

    fun getGeofenceSettings() = userPreference.accessGeofenceData

    suspend fun setUemSettings(uemSetting: UemSetting) {
        userPreference.saveLocationTrackingData(uemSetting)
    }

    fun getUemSettings() = userPreference.accessLocationTrackingData

    suspend fun setDeviceId(deviceId: String) {
        userPreference.saveDeviceId(deviceId)
    }

    suspend fun getInitialLocationSetting(enrollmentData: DeviceEnrollmentData) =
        safeApiCall { locationApi.getInitialSettings(enrollmentData) }

    private fun getOrganisationData() = userPreference.accessOrganizationData

    suspend fun setOrganizationData(organizationData: OrganizationData) {
        userPreference.saveOrganizationData(organizationData)
    }

    fun startSession() {
        socketClient.startSession()
//        socketClient.establishConnection()
    }

    fun isSocketConnected() = socketClient.isSocketConnected()

    fun sendMessageToSocket(event: String = "", message: Any) {
        Log.d("WEBSOCKET", "sendMessageToSocketREPO: $message")
        if(!isSocketConnected()) socketClient.connect(message)
        socketClient.sendMessageToSocket(message)
    }

    fun closeSocketConnection() {
        socketClient.closeConnection()
    }

    override suspend fun onMessageReceived(event: String, message: String) {

        val socketResponse = try {
            JSONObject(message)
        } catch (e: Exception) {
            Log.d("FATAL", "onMessageReceived: ${e.message}")
            null
        }
        val orgId = socketResponse?.getString("org_id")
        val enterpriseId = socketResponse?.getString("enterprise_id")
        Log.d("WEBSOCKET", "socketResponse:$event $socketResponse")
        if (event == "NEW_SETTING_CHANGE") {
            userPreference.saveOrganizationData(
                OrganizationData(
                    orgId,
                    enterpriseId
                )
            )
            if (socketResponse != null) {
                typeWiseDataStoreAndService(socketResponse)
            }
        }
        if (event == "INITIAL") {

            try {
                if (socketResponse != null) {
                    val locationTrackingConfig =
                        socketResponse.getJSONObject("location_tracking_configuration")
//                val batteryTrackingConfig =
//                    socketResponse.getJSONObject("battery_tracking_configuration")
//                val displaySettingsConfig =
//                    socketResponse.getJSONObject("display_setting_configuration")
                    userPreference.saveOrganizationData(
                        OrganizationData(
                            orgId,
                            enterpriseId
                        )
                    )

//                    userPreference.saveLocationTrackingData(
//                        Configuration.LocationTrackingConfig(
//                            locationTrackingConfig.getBoolean("enabled"),
//                            locationTrackingConfig.getBoolean("history_enabled"),
//                            locationTrackingConfig.getInt("range")
//                        )
//                    )
//                userPreference.saveBatteryTrackingData(
//                    Configuration.BatteryTrackingConfig(
//                        batteryTrackingConfig.getBoolean("enabled"),
//                        batteryTrackingConfig.getInt("hour"),
//                        batteryTrackingConfig.getInt("minutes"),
//                        batteryTrackingConfig.getString("type")
//                    )
//                )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("WEBSOCKET", "onMessageReceivedyyuyu: ${e.message}")
            }
        }
    }

    private suspend fun typeWiseDataStoreAndService(socketResponse: JSONObject) {
        val type = socketResponse.getString("type")

        when (type) {

            "GEO_TRACKING" -> {
                val locationTrackingConfig = socketResponse.getJSONObject("configuration")
//                userPreference.saveLocationTrackingData(
//                    Configuration.LocationTrackingConfig(
//                        locationTrackingConfig.getBoolean("enabled"),
//                        locationTrackingConfig.getBoolean("history_enabled"),
//                        locationTrackingConfig.getInt("range")
//                    )
//                )
            }

            "BATTERY_TRACKING" -> {
                val batteryTrackingConfig = socketResponse.getJSONObject("configuration")
                userPreference.saveBatteryTrackingData(
                    Configuration.BatteryTrackingConfig(
                        batteryTrackingConfig.getBoolean("enabled"),
                        batteryTrackingConfig.getInt("hour"),
                        batteryTrackingConfig.getInt("minutes"),
                        batteryTrackingConfig.getString("type")
                    )
                )
            }
        }
    }

    private fun getBatteryData(): BatteryData = batteryDataHelper.getBatteryData()

    suspend fun sendBatteryDataPeriodically() {

        userPreference.accessBatteryTrackingData.collect { batteryDetail ->

            if (batteryDetail != null) {
                val runnable = runOnInterval({
                    CoroutineScope(Dispatchers.IO).launch {
                        val organizationData = getOrganisationData().first()
                        val deviceId = getDeviceId().first() ?: "3a228bceb1e5cc33"

                        if (organizationData != null) {
                            val batteryData = BatteryTrackingDetails(
                                organizationData.org_id,
                                organizationData.enterprise_id,
                                deviceId,
                                batteryData = getBatteryData(),
                                timeStamp = System.currentTimeMillis()
                            )
                            if (batteryDetail.enabled) {
                                sendMessageToSocket(
                                    Constant.BATTERY_TRACKING,
                                    Gson().toJson(batteryData)
                                )
                            }
                        }
                    }
                    Log.d("FATAL", "run: ${getBatteryData()}")

                    // Schedule the next connection after a certain interval

                }, hourAndMinutesToMillis(batteryDetail.hour, batteryDetail.minutes))

                if (!batteryDetail.enabled) {
                    Handler(Looper.getMainLooper()).removeCallbacks(runnable)
                }
            }
        }
    }

    suspend fun insertLocationData(locationData: LocationEntity) {
        locationDao.insertLocationData(locationData)
    }

    suspend fun getLocationData(): List<Property> {
        return locationDao.getLocationData().map {
            it.toProperty()
        }
    }

    suspend fun deleteLocationData() {
        locationDao.deleteLocationData()
    }

    suspend fun getLocationDataByType(type: String): List<Property> {
        return locationDao.getLocationDataByType(type).map {
            it.toProperty()
        }
    }

    suspend fun getPreviousDayLocationData(type: String): List<Property> {
        val calendar = Calendar.getInstance()

        // Set to the start of today and then go back to the start of yesterday
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endOfYesterday = calendar.timeInMillis - 1

        // Set to the start of yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = calendar.timeInMillis
        Log.d(TAG, "getPreviousDayLocationData: $startOfYesterday, $endOfYesterday")
        return locationDao.getPreviousDayLocationDataByType(type, startOfYesterday, endOfYesterday)
            .map { it.toProperty() }
    }

    suspend fun sendLocationData(
        locationData: List<LocationDataRequest>,
        type: String
    ): Resource<LocationDataResponse> {
        return safeApiCall {
            if (type == Constant.LOCATION_HISTORY) {
                locationApi.sendLocationHistory(locationData)
            } else {
                locationApi.sendLocationBreadCrumbs(locationData)
            }
        }
    }

    //    suspend fun getFenceData(boundaryID: String): Resource<GeoJson> {
//        return safeApiCall { locationApi.getGeofenceData(FenceDataRequest(boundaryID)) }
//    }
    suspend fun getFenceData(enterpriseId: String, deviceId: String, fenceId: String): Fencing {
        val fenceDataRequest = FenceDataRequest(
            enterpriseId,
            deviceId,
            fenceId
        )
        return locationApi.getGeofenceData(fenceDataRequest)
    }

    suspend fun getBoundaryCoordinates(boundaryID: String) =
        locationApi.getBoundaryCoordinates(BoundaryCoordinatesRequest(boundaryID))

    suspend fun insertFenceData(geoJson: GeoJSON) {
        locationDao.insertFenceData(geoJson.toGeoJSONEntity())
    }

    suspend fun getAllFenceData() = locationDao.getFenceData().map { it.toGeoJSON() }

    suspend fun getFenceDataByFenceId(fenceId: String) =
        locationDao.getFenceDataByFenceId(fenceId).toGeoJSON()

    suspend fun getFenceDataByBoundaryId(boundaryId: String) =
        locationDao.getFenceDataByBoundaryId(boundaryId).map { it.toGeoJSON() }

    suspend fun deleteFenceData() {
        locationDao.deleteFenceData()
    }

    suspend fun deleteFenceDataByBoundaryId(boundaryId: String) {
        locationDao.deleteFenceDataByBoundaryId(boundaryId)
    }

    suspend fun logFence(fenceLog: FenceLog) = safeApiCall { locationApi.logFences(fenceLog) }
}