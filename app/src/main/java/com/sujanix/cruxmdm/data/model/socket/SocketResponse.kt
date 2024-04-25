package com.sujanix.cruxmdm.data.model.socket

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SocketResponse(
    val org_id: String,
    val enterprise_id: String,
    val type: String,
    val configuration: @RawValue Configuration,
    val status: Boolean
) : Parcelable