package com.sujanix.cruxmdm.features.enrollment.data.model.response

data class GenerateQrCodeResponse(
    val token_id: String = "",
    val enrollment_link: String = "",
    val enrollment_token: EnrollmentToken = EnrollmentToken(),
    val qr_url: String = "",
    val message: String = "",
    val status: Boolean = false
)