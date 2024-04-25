package com.sujanix.cruxmdm.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User_table")
data class User (
    val name: String,
    val phone: String,
    val email:String,
    val imei: String = "",
    val serialNo: String = "",
    val androidId: String,
    val enterpriseId: String = "enterprises/LC04cb8wma",
    val deviceId: String = "",
    @PrimaryKey
    val id: Int = 0
)
