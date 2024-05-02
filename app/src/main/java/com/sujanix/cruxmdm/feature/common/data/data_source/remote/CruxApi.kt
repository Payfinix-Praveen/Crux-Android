package com.sujanix.cruxmdm.feature.common.data.data_source.remote

import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.feature.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.feature.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.feature.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CruxApi {

    companion object {
//        const val BASE_URL = "http://192.168.0.117:5000/app/"
        const val BASE_URL = "https://crux-uem-backend.onrender.com/app/"
    }

    @POST("praveencruxapi")
    suspend fun registerUser(
        @Body registerData: RegisterData
    ): RegisterResponse

    @POST("praveencruxapi")
    suspend fun loginUser(
        @Body loginData: LoginData
    ): RegisterResponse

    @POST("app_list_android")
    suspend fun getEnterpriseAppList(
        @Body enterprise_id: OrganizationData
    ): List<ApplicationDataApi>

    @Multipart
    @POST("distributed_self_hosted_app")
    suspend fun getSelfHostedApplications(
        @Part("enterprise_id") enterpriseId: RequestBody,
        @Part("device_id") deviceId: RequestBody
    ): SelfHostedApplication

}