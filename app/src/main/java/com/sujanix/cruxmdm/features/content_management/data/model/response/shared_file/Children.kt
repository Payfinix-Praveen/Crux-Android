package com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Children(
    val file_size: Long = 0,
    val is_favourite: Boolean = false,
    val name: String = "",
    val shared_with: List<SharedWithX> = listOf(),
    val tags: List<String> = listOf(),
    val type: String = "",
    var fileKey: String = "",
    var isDownloaded: Boolean = false
): Parcelable