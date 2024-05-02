package com.sujanix.cruxmdm.feature.auth.data.repository

import android.content.Context
import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.feature.common.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.feature.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.feature.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.feature.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.feature.common.data.repository.BaseRepository
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import com.sujanix.cruxmdm.feature.common.utlis.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val appDao: ApplicationDao,
    private val userPreference: UserPreferences
): BaseRepository() {

    suspend fun registerUser(registerData: RegisterData): RegisterResponse {
        return api.registerUser(registerData)
    }

    suspend fun loginUser(loginData: LoginData): Resource<RegisterResponse> {
        return safeApiCall {
            api.loginUser(loginData)
        }
    }
}