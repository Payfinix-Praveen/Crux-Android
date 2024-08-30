package com.sujanix.cruxmdm.features.core.data.model.request

import com.google.gson.annotations.SerializedName

data class LocationDataRequest(
    @SerializedName("device_id")val deviceId: String = "",
    @SerializedName("enterprise_id")val enterpriseId: String = "",
    val properties: List<Property> = listOf(),
    @SerializedName("tracking_type")val trackingType: String = "",
    @SerializedName("user_id")val userId: String = "",
    @SerializedName("app_sync_time")val appSyncTime: String = ""
)