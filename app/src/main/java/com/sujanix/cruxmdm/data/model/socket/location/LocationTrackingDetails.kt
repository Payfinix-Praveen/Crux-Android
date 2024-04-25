package com.sujanix.cruxmdm.data.model.socket.location

data class LocationTrackingDetails(
    val org_id: String? = null,
    val enterprise_id: String? = null,
    val device_id: String? = null,
    val location: Location,
    val timeStamp: Long,
    val history: Boolean
)
