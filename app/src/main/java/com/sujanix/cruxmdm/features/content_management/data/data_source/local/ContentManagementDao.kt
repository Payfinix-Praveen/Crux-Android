package com.sujanix.cruxmdm.features.content_management.data.data_source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContentManagementDao {

    @Insert
    suspend fun insertFileData(fileData: FileDataEntity)

    @Query("SELECT * FROM file_data_table")
    suspend fun getAllFileData(): List<FileDataEntity>

    @Query("SELECT * FROM file_data_table WHERE `key` = :fileKey")
    suspend fun getFileByKey(fileKey: String): FileDataEntity?

    @Query("DELETE FROM file_data_table")
    suspend fun deleteAllFileData()

    @Query("DELETE FROM file_data_table WHERE `key` = :fileKey")
    suspend fun deleteFileByKey(fileKey: String)
}