package com.sujanix.cruxmdm.features.notification_policyActivity.data.data_source.local

import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val title: String,
    val message: String,
    val image: String,
    val created: Long
) {
    val createdDateFormatted: String
        get() {
            val date = Date(created)
            val format = SimpleDateFormat("dd-MMM", Locale.ENGLISH)
            return format.format(date)
        }
}
