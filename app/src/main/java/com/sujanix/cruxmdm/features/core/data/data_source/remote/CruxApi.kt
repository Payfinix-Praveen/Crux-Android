package com.sujanix.cruxmdm.features.core.data.data_source.remote

import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.features.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.features.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.features.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.features.auth.data.model.response.EnterpriseData
import com.sujanix.cruxmdm.features.auth.data.model.response.LoginResponse
import com.sujanix.cruxmdm.features.core.data.model.response.ApkSignedUrlResponse
import com.sujanix.cruxmdm.features.content_management.data.model.request.DownloadSharedFileRequest
import com.sujanix.cruxmdm.features.content_management.data.model.response.DownloadSharedFileResponse
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.SharedJSONFileDto
import com.sujanix.cruxmdm.features.enrollment.data.model.request.EnrollmentData
import com.sujanix.cruxmdm.features.enrollment.data.model.response.EnterpriseGroupListDataResponse
import com.sujanix.cruxmdm.features.enrollment.data.model.response.GenerateQrCodeResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CruxApi {

    companion object {
//        const val BASE_URL = "http://192.168.0.158:5000/" //SEGGU SERVER
//        const val BASE_URL = "http://192.168.0.218:5000/" //SANJEEV SERVER
//        const val BASE_URL = "http://192.168.0.120:5000/" //DANISH SERVER
//        const val BASE_URL = "http://192.168.0.224:5000/" //ARAVIND SERVER
//        const val BASE_URL = "https://crux-uem-backend.onrender.com/" //PRODUCTION RENDER SERVER
//        const val BASE_URL = "https://lsi9z17ptk.execute-api.us-east-1.amazonaws.com/dev123/" //PRODUCTION SERVER
        const val BASE_URL = "https://02fhoaa58j.execute-api.us-east-1.amazonaws.com/cruxdev/" //DEV SERVER
    }

    // USER MANAGEMENT APIs
    @POST("device_user/register_device_user")
    suspend fun registerUser(
        @Body registerData: RegisterData
    ): RegisterResponse

    @POST("device_user/login_device_user")
    suspend fun loginUser(
        @Body loginData: LoginData
    ): LoginResponse

    @GET("groups/getenterprise")
    suspend fun getEnterpriseList(): EnterpriseData

    @POST("groups/get_enterprise_groups")
    suspend fun getEnterpriseGroupList(
        @Body enterprise_id: OrganizationData
    ): EnterpriseGroupListDataResponse

    @POST("emm/enrollnew")
    suspend fun generateQrCode(
        @Body enrollData: EnrollmentData
    ): GenerateQrCodeResponse

    // APP CATALOGUE APIs
    @POST("app/app_list_android")
    suspend fun getEnterpriseAppList(
        @Body enterprise_id: OrganizationData
    ): List<ApplicationDataApi>

    @Multipart
    @POST("app/distributed_self_hosted_app")
    suspend fun getSelfHostedApplications(
        @Part("enterprise_id") enterpriseId: RequestBody,
        @Part("device_id") deviceId: RequestBody
    ): SelfHostedApplication

    @Multipart
    @POST("app/get_apk_signed_url")
    suspend fun getApkSignedUrl(
        @Part("enterprise_id") enterpriseId: RequestBody,
        @Part("package_name") packageName: RequestBody
    ): ApkSignedUrlResponse

    // CONTENT MANAGEMENT APIs
    @POST("content/get_shared_file")
    suspend fun getSharedFileJson(
        @Body organizationData: OrganizationData
    ): SharedJSONFileDto

    @POST("content/download_shared_file")
    suspend fun getSharedFileUrl(
        @Body fileKey: DownloadSharedFileRequest
    ): DownloadSharedFileResponse
}