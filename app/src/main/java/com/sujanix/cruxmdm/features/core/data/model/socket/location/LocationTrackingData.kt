package com.sujanix.cruxmdm.features.core.data.model.socket.location

data class LocationTrackingData(
    val device_id: String = "",
    val enterprise_id: String = "",
    val properties: Properties = Properties(),
    val tracking_type: String = "",
    val user_id: String = ""
)