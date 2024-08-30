package com.sujanix.cruxmdm.features.auth.data.model.request

data class RegisterData(
    val aadhar_number: String = "",
    val device_user_add_type: String = "Self Register",
    val device_user_photo: String = "",
    val email: String = "",
    val org_id: String = "",
    val enterprise_id: String = "",
    val is_aadhar_verified: Boolean = false,
    val is_email_verified: Boolean = true,
    val is_phone_verified: Boolean = false,
    val mobile_number: String = "",
    val name: String = ""
)