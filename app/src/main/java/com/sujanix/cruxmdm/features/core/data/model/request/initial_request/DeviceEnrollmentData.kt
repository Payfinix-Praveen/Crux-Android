package com.sujanix.cruxmdm.features.core.data.model.request.initial_request

data class DeviceEnrollmentData(
    val device_id: String = "",
    val enrollment_type: String = "",
    val enterprise_id: String = "",
    val org_id: String = "",
    val user_id: String = ""
)