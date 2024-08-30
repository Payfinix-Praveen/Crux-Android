package com.sujanix.cruxmdm.features.core.data.data_source.remote

import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.fencing.GeoJson
import com.sujanix.cruxmdm.features.core.data.model.fencing.fence_log.FenceLog
import com.sujanix.cruxmdm.features.core.data.model.request.BoundaryCoordinatesRequest
import com.sujanix.cruxmdm.features.core.data.model.request.FenceDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.LocationDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.initial_request.DeviceEnrollmentData
import com.sujanix.cruxmdm.features.core.data.model.response.DefaultResponse
import com.sujanix.cruxmdm.features.core.data.model.response.LocationDataResponse
import com.sujanix.cruxmdm.features.core.data.model.response.initial_location_setting.InitialLocationSetting
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApi {

    companion object {
        const val LOCATION_BASE_URL = "https://crux-location-backend.onrender.com/location-management/"
    }

    @POST("getIntialsettings")
    suspend fun getInitialSettings(
        @Body enrollmentData: DeviceEnrollmentData
    ): InitialLocationSetting

    @POST("addlocationhistoryv2")
    suspend fun sendLocationHistory(
        @Body locationHistory: List<LocationDataRequest>
    ): LocationDataResponse

    @POST("addbreadcrumbsv2")
    suspend fun sendLocationBreadCrumbs(
        @Body breadCrumbs: List<LocationDataRequest>
    ): LocationDataResponse

    @POST("getGeoFencing")
    suspend fun getGeofenceData(
        @Body fenceDataRequest: FenceDataRequest
    ): Fencing

    @POST("getCoordinates")
    suspend fun getBoundaryCoordinates(
        @Body boundaryId: BoundaryCoordinatesRequest
    ): GeoJson

    @POST("log-fences")
    suspend fun logFences(
        @Body fenceData: FenceLog
    ): DefaultResponse

}