package com.sujanix.cruxmdm.socket.model

data class TrackingData(
    val device_id: String = "",
    val direction: String = "",
    val enterprise_id: String = "",
    val geo_coding: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val speed: String = "",
    val timestamp: String = ""
)