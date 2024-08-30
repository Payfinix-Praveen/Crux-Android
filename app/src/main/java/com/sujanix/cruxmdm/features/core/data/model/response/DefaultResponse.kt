package com.sujanix.cruxmdm.features.core.data.model.response

data class DefaultResponse(
    val errorcode: Int,
    val message: String,
    val status: Boolean
)