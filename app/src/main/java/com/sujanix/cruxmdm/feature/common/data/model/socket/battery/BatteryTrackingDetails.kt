package com.sujanix.cruxmdm.feature.common.data.model.socket.battery

import com.sujanix.cruxmdm.feature.common.data.model.socket.battery.BatteryData

data class BatteryTrackingDetails(
    val org_id: String? = null,
    val enterprise_id: String? = null,
    val device_id: String? = null,
    val batteryData: BatteryData,
    val timeStamp: Long
)
