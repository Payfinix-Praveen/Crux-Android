package com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requested_app_table")
data class RequestedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val packageName: String,
    val appStatus: AppStatus,
    val requestDate: String
)

data class RequestedApp(
    val name: String,
    val packageName: String,
    val appStatus: AppStatus,
    val requestDate: String
)

fun RequestedApp.toRequestedAppEntity() = RequestedAppEntity(
    name = name,
    packageName = packageName,
    appStatus = appStatus,
    requestDate = requestDate
)

fun RequestedAppEntity.toRequestedApp() = RequestedApp(
    name = name,
    packageName = packageName,
    appStatus = appStatus,
    requestDate = requestDate
)

enum class AppStatus {
    PENDING,
    APPROVED,
    REJECTED
}