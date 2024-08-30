package com.sujanix.cruxmdm.features.core.data.model.response.initial_location_setting

data class InitialLocationSetting(
    val `data`: Data = Data(),
    val errorcode: Int = 0,
    val message: String = "",
    val status: Boolean = false
)