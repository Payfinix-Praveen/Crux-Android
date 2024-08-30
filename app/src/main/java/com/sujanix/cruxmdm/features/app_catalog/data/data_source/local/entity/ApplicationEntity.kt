package com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application

@Entity(tableName = "application_table")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val type: String? = null,
    val name: String? = null,
//    val icon: Drawable? = null,
    val pkg: String? = null,
    val url: String? = null,
    val apkPath: String? = null,
    val version: String? = null,
    val versionCode: Int? = null,
    val iconUrl: String? = null,
    val isInstalled: Boolean = false,
    val isRemove: Boolean = false,
    val isDownloaded: Boolean = false,
    val isUpdateAvailable: Boolean = false
){
    companion object {
        fun ApplicationEntity.toApplication(): Application {
            return Application(
                type = type,
                name = name,
                pkg = pkg,
                url = url,
                apkPath = apkPath,
                version = version,
                versionCode = versionCode,
                iconUrl = iconUrl,
                isInstalled = isInstalled,
                isRemove = isRemove,
                isDownloaded = isDownloaded,
                isUpdateAvailable = isUpdateAvailable
            )
        }
    }
}
