package com.sujanix.cruxmdm.data.model

data class User (
    val name: String,
    val phone: String,
    val email:String,
    val imei: String = "",
    val serialNo: String = ""
)
