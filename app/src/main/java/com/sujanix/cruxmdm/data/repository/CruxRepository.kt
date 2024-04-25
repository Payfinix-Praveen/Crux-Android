package com.sujanix.cruxmdm.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.sujanix.cruxmdm.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.data.model.request.OrganizationData
import com.sujanix.cruxmdm.data.model.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.data.model.request.LoginData
import com.sujanix.cruxmdm.data.model.request.RegisterData
import com.sujanix.cruxmdm.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.data.model.socket.Configuration
import com.sujanix.cruxmdm.data.model.socket.battery.BatteryData
import com.sujanix.cruxmdm.data.model.socket.battery.BatteryTrackingDetails
import com.sujanix.cruxmdm.socket.SocketClient
import com.sujanix.cruxmdm.socket.SocketClientImp
import com.sujanix.cruxmdm.util.Constant
import com.sujanix.cruxmdm.util.InstallUtils.checkApplicationDownloadAndInstallStatus
import com.sujanix.cruxmdm.util.Resource
import com.sujanix.cruxmdm.util.UserPreferences
import com.sujanix.cruxmdm.util.battery.BatteryDataHelper
import com.sujanix.cruxmdm.util.hourAndMinutesToMillis
import com.sujanix.cruxmdm.util.isApkDownloaded
import com.sujanix.cruxmdm.util.isPackageInstalled
import com.sujanix.cruxmdm.util.runOnInterval
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class CruxRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val appDao: ApplicationDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
) : BaseRepository(), SocketClientImp.SocketListener {

    private val socketClient: SocketClient by lazy {
        SocketClientImp(context, userPreference, this)
    }

    suspend fun registerUser(registerData: RegisterData): RegisterResponse {
        return api.registerUser(registerData)
    }

    suspend fun loginUser(loginData: LoginData): Resource<RegisterResponse> {
        return safeApiCall {
            api.LoginUser(loginData)
        }
    }

    fun getUserData() = userPreference.accessUserData

    suspend fun saveUserData(registerData: RegisterData) {
        userPreference.saveUserData(registerData)
    }

    suspend fun clearUserData() {
        userPreference.clear()
    }

    fun getEnrollmentDevice() = userPreference.getDeviceEnrollment

    suspend fun setDeviceEnrolled() {
        userPreference.setDeviceEnrolled()
    }

    fun getDeviceId() = userPreference.accessDeviceId

    suspend fun setDeviceId(deviceId: String) {
        userPreference.saveDeviceId(deviceId)
    }

    fun getOrganisationData() = userPreference.accessOrganizationData

    suspend fun setOrganizationData(organizationData: OrganizationData) {
        userPreference.saveOrganizationData(organizationData)
    }

    fun startSession() {
        socketClient.startSession()
//        socketClient.establishConnection()
    }

    fun isSocketConnected() = socketClient.isSocketConnected()

    fun sendMessageToSocket(event: String, message: Any) {
        Log.d("WEBSOCKET", "sendMessageToSocketREPO: $message")
        socketClient.sendMessageToSocket(event, message)
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

                    userPreference.saveLocationTrackingData(
                        Configuration.LocationTrackingConfig(
                            locationTrackingConfig.getBoolean("enabled"),
                            locationTrackingConfig.getBoolean("history_enabled"),
                            locationTrackingConfig.getInt("range")
                        )
                    )
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
                userPreference.saveLocationTrackingData(
                    Configuration.LocationTrackingConfig(
                        locationTrackingConfig.getBoolean("enabled"),
                        locationTrackingConfig.getBoolean("history_enabled"),
                        locationTrackingConfig.getInt("range")
                    )
                )
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
                            if (batteryDetail.enabled)
                                sendMessageToSocket(
                                    Constant.BATTERY_TRACKING,
                                    Gson().toJson(batteryData)
                                )
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

    suspend fun getApplicationList(organisationData: OrganizationData): Resource<List<ApplicationDataApi>> {
        return safeApiCall { api.getEnterpriseAppList(organisationData) }
    }

    suspend fun getSelfHostedApplicationList(organisationData: OrganizationData): Resource<SelfHostedApplication> {
        return safeApiCall {
            api.getSelfHostedApplications(
                enterpriseId = organisationData.enterprise_id!!.makeRequestBody(),
                deviceId = organisationData.device_id!!.makeRequestBody()
            )
        }
    }

    suspend fun insertApplication(application: Application) {
        val applicationEntity = checkApplicationDownloadAndInstallStatus(context, application)
        Log.d("FATAL", "insertApplication: $applicationEntity")
        appDao.insertApplication(applicationEntity)
    }

    suspend fun getAllApplication() = appDao.getAllApplications()

    suspend fun deleteAllApplication(){
        appDao.deleteAllApplications()
    }

    suspend fun apkDownloaded(filePath: String, packageName: String, downloaded: Boolean) {
        appDao.setApplicationDownloaded(filePath, packageName, downloaded)
    }
}