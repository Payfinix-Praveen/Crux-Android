package com.sujanix.cruxmdm.features.auth.data.model.response

data class DeviceGroupData(
    val data: List<GroupData>,
    val msg: String,
    val status: Boolean
)