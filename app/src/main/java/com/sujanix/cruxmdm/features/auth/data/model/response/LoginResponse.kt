package com.sujanix.cruxmdm.features.auth.data.model.response

data class LoginResponse(
    val access_token: String = "",
    val device_user: DeviceUserData? = null,
    val message: String = "",
    val status: Boolean = false
)