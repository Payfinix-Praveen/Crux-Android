package com.sujanix.cruxmdm.features.core.data.model.socket.battery

data class BatteryTrackingDetails(
    val org_id: String? = null,
    val enterprise_id: String? = null,
    val device_id: String? = null,
    val batteryData: BatteryData,
    val timeStamp: Long
)
