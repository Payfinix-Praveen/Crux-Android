package com.sujanix.cruxmdm.features.auth.data.repository

import android.content.Context
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.features.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.auth.data.model.response.LoginResponse
import com.sujanix.cruxmdm.features.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.battery.BatteryDataHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val locationApi: LocationApi,
    private val appDao: ApplicationDao,
    private val locationDao: LocationDataDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
): CruxRepository(context, api, locationApi, appDao, locationDao, userPreference, batteryDataHelper){

    suspend fun registerUser(registerData: RegisterData): Resource<RegisterResponse> {
        return safeApiCall {
            api.registerUser(registerData)
        }
    }

    suspend fun loginUser(loginData: LoginData): Resource<LoginResponse> {
        return safeApiCall {
            api.loginUser(loginData)
        }
    }

    suspend fun saveDeviceUserData(data: DeviceUserData) {
        userPreference.saveUserData(data)
    }

    fun getDeviceUserData() = userPreference.accessDeviceUserData

    suspend fun getEnterpriseList() = safeApiCall {
        api.getEnterpriseList()
    }
}