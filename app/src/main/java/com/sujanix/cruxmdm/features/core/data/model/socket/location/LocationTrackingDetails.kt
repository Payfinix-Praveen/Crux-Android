package com.sujanix.cruxmdm.features.core.data.model.socket.location

data class LocationTrackingDetails(
    val org_id: String? = null,
    val enterprise_id: String? = null,
    val device_id: String? = null,
    val coordinate: Coordinate,
    val timeStamp: Long,
    val history: Boolean
)

