package com.sujanix.cruxmdm.features.core.data.model.request

import com.google.gson.annotations.SerializedName

data class FenceDataRequest(
    @SerializedName("enterprise_id")
    val enterpriseId: String,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("fence_id")
    val fenceId: String
)
