package com.sujanix.cruxmdm.features.core.data.model.response.initial_location_setting

import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting

data class Data(
    val breadcrumbs: String? = null,
    val fencing: List<Fencing>? = null,
    val uemSetting: UemSetting = UemSetting()
)