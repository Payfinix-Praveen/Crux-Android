package com.sujanix.cruxmdm.features.enrollment.data.model.response

data class EnterpriseGroupListDataResponse(
    val data: List<GroupData> = listOf(),
    val msg: String = "",
    val status: Boolean = false
)