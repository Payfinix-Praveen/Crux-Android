package com.sujanix.cruxmdm.data.model.socket.battery

import com.sujanix.cruxmdm.data.model.socket.battery.BatteryData

data class BatteryTrackingDetails(
    val org_id: String? = null,
    val enterprise_id: String? = null,
    val device_id: String? = null,
    val batteryData: BatteryData,
    val timeStamp: Long
)
