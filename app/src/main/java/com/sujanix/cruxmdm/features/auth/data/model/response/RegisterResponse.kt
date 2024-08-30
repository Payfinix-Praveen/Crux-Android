package com.sujanix.cruxmdm.features.auth.data.model.response

data class RegisterResponse(
    val Message: String,
    val Status: Boolean,
    val device_user_id: String
)