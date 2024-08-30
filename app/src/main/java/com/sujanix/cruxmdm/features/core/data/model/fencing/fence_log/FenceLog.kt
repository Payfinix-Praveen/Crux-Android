package com.sujanix.cruxmdm.features.core.data.model.fencing.fence_log

import com.google.gson.annotations.SerializedName

data class FenceLog(
    val alerts: List<String> = listOf("LOG", "ISSUE_ALERT", "ALERT_USER", "ADMIN_ALERT", "LOCK_DOWN"),
    @SerializedName("current_location")
    val currentLocation: CurrentLocation,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("email_addrs")
    val email: String,
    @SerializedName("enterprise_id")
    val enterpriseId: String,
    @SerializedName("fence_id")
    val fenceId: Int,
    @SerializedName("inside_fence")
    val insideFence: Boolean = false,
)
