package com.sujanix.cruxmdm.features.auth.data.model.request

data class LoginData(
    val email: String,
    val fcm_token: String
)