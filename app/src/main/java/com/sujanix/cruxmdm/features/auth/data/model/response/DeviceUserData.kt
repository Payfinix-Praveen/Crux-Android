package com.sujanix.cruxmdm.features.auth.data.model.response

data class DeviceUserData(
    val aadhar_number: String = "",
    val device_user_id: String = "",
    val email: String = "",
    val enterprise_id: String = "",
    val fullname: String = "",
    val org_id: String = "",
    val org_name: String = "",
    val phone_number: String = "",
    val validate_inventory: Boolean = false,
    val groupId: String = "",
    val token_id: String = "TKN${System.currentTimeMillis()}",
)