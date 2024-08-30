package com.sujanix.cruxmdm.features.core.utlis

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.core.data.model.socket.Configuration
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    val preferences = appContext.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE)
    val editor = preferences.edit()
    val gson = Gson()

    val accessDeviceUserData: Flow<DeviceUserData?>
        get() = appContext.dataStore.data.map { preferences ->
            val userDataString = preferences[USER_DATA]
            gson.fromJson(userDataString, DeviceUserData::class.java)
        }

    suspend fun saveUserData(deviceUserData: DeviceUserData) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_DATA] = gson.toJson(deviceUserData)
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val accessBreadcrumbData: Flow<Boolean?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[BREADCRUMB_DATA]
        }

    suspend fun saveBreadcrumbData(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[BREADCRUMB_DATA] = enabled
        }
    }

    suspend fun setDeviceEnrolled() {
        appContext.dataStore.edit { preferences ->
            preferences[ENROLLMENT_STATUS] = true
        }
    }

    val getDeviceEnrollment: Flow<Boolean?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ENROLLMENT_STATUS]
        }

    val accessGeofenceData: Flow<Fencing?>
        get() = appContext.dataStore.data.map { preferences ->
            val userDataString = preferences[GEOFENCE_DATA]
            gson.fromJson(userDataString, Fencing::class.java)
        }

    suspend fun saveGeofenceData(geofenceData: Fencing){
        appContext.dataStore.edit { preference ->
            preference[GEOFENCE_DATA] = gson.toJson(geofenceData)
        }
    }

    val accessDeviceId: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[DEVICE_ID]
        }

    suspend fun saveDeviceId(deviceId: String) {
        appContext.dataStore.edit { preferences ->
            preferences[DEVICE_ID] = deviceId
        }
    }

    val accessOrganizationData: Flow<OrganizationData?>
        get() = appContext.dataStore.data.map { preferences ->
            val organizationData = preferences[ORGANIZATION_DATA]
            gson.fromJson(organizationData, OrganizationData::class.java)
        }

    suspend fun saveOrganizationData(orgData: OrganizationData){
        appContext.dataStore.edit { preferences ->
            preferences[ORGANIZATION_DATA] = gson.toJson(orgData)
        }
    }

    val accessLocationTrackingData: Flow<UemSetting>
        get() = appContext.dataStore.data.map { preferences ->
            val locationTrackingData = preferences[LOCATION_TRACKING_DATA]
            gson.fromJson(locationTrackingData, UemSetting::class.java)
        }

    suspend fun saveLocationTrackingData(locationTrackingData: UemSetting){
        appContext.dataStore.edit { preferences ->
            preferences[LOCATION_TRACKING_DATA] = gson.toJson(locationTrackingData)
        }
    }

    val accessBatteryTrackingData: Flow<Configuration.BatteryTrackingConfig?>
        get() = appContext.dataStore.data.map { preferences ->
            val batteryTrackingData = preferences[BATTERY_TRACKING_DATA]
            gson.fromJson(batteryTrackingData, Configuration.BatteryTrackingConfig::class.java)
        }

    suspend fun saveBatteryTrackingData(batteryTrackingData: Configuration.BatteryTrackingConfig){
        appContext.dataStore.edit { preferences ->
            preferences[BATTERY_TRACKING_DATA] = gson.toJson(batteryTrackingData)
        }
    }

    companion object {
        private val USER_DATA = stringPreferencesKey("USER_DATA")
        private val ENROLLMENT_STATUS = booleanPreferencesKey("ENROLLMENT_STATUS")
        private val DEVICE_ID = stringPreferencesKey("DEVICE_ID")
        private val ORGANIZATION_DATA = stringPreferencesKey("ORGANIZATION_DATA")

        private val BATTERY_TRACKING_DATA = stringPreferencesKey("BATTERY_TRACKING_DATA")

        //LOCATION_MANAGEMENT_DATA
        private val BREADCRUMB_DATA = booleanPreferencesKey("BREADCRUMB_DATA")
        private val GEOFENCE_DATA = stringPreferencesKey("GEOFENCE_DATA")
        private val LOCATION_TRACKING_DATA = stringPreferencesKey("LOCATION_TRACKING_DATA")
    }
}