package com.sujanix.cruxmdm.features.core.data.model.location_history

data class UemSetting(
    val enable: Boolean = false,
    val history_enabled: Boolean = true,
    val range: Int = 0,
    val track_when_lost: Boolean = false
)