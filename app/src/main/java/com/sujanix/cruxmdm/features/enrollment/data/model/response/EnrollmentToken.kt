package com.sujanix.cruxmdm.features.enrollment.data.model.response

data class EnrollmentToken(
    val additionalData: String = "",
    val duration: String = "",
    val expirationTimestamp: String = "",
    val name: String = "",
    val oneTimeOnly: Boolean = false,
    val policyName: String = "",
    val qrCode: String = "",
    val value: String = ""
)