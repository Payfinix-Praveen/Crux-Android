package com.sujanix.cruxmdm.feature.auth.data.model.request

data class LoginData(
    val app_email: String,
    val app_password: String,
    val geo_lat: String,
    val geo_long: String,
    val network_info: String,
    val android_id: String,
//    val android_id: String,
    val enterprise_id: String = "enterprises/LC04cb8wma"
)