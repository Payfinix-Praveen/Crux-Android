package com.sujanix.cruxmdm.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sujanix.cruxmdm.util.isPackageInstalled

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
    val isDownloaded: Boolean = false
)
