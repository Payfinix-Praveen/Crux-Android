package com.sujanix.cruxmdm.data.model.request

data class Temp(
    val password: String,
    val app_email: String,
//    val phone_number: String,
//    val enterprise_id: String,
    val geo_lat: String,
    val geo_long: String,
    val network_info: String
)