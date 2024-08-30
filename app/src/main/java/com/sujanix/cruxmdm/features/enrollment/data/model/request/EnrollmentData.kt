package com.sujanix.cruxmdm.features.enrollment.data.model.request

import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData

data class EnrollmentData(
    val additionalData: DeviceUserData,
    val duration: String = "",
    val enterpriseName: String = "",
    val oneTimeOnly: Boolean = false,
    val policyName: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val user_role: String = "App",
    val timeDifferenceInSeconds: String = ""
)