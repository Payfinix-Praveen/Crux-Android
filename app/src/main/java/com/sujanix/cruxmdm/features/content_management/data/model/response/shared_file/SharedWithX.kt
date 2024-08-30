package com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedWithX(
    val device_id: String? = null,
    val device_model: String? = null,
    val device_name: String? = null,
    val enrollment_type: String? = null,
    val enterprise_id: String = "",
    val group_id: String? = null,
    val group_id_devices: Int? = null,
    val name: String? = null,
    val shared_time: String? = null,
    val platform_type: String? = null,
    val serial_number: String? = null,
    val type: String = ""
): Parcelable