package com.sujanix.cruxmdm.features.content_management.data.data_source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_data_table")
data class FileDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val key: String
)

data class FileData(
    val name: String,
    val path: String,
    val key: String
)

fun FileDataEntity.toFileData(): FileData {
    return FileData(
        name = name,
        path = path,
        key = key
    )
}

fun FileData.toFileDataEntity(): FileDataEntity {
    return FileDataEntity(
        name = name,
        path = path,
        key = key
    )
}