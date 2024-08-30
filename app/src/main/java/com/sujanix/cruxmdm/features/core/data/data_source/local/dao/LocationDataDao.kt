package com.sujanix.cruxmdm.features.core.data.data_source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSONEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.LocationEntity

@Dao
interface LocationDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationData(locationEntity: LocationEntity)

    @Query("SELECT * FROM location_table")
    suspend fun getLocationData(): List<LocationEntity>

    @Query("DELETE FROM location_table")
    suspend fun deleteLocationData()

    @Query("SELECT * FROM location_table WHERE type = :type")
    suspend fun getLocationDataByType(type: String): List<LocationEntity>

    @Query("SELECT * FROM location_table WHERE type = :type AND isPushed = 0")
    suspend fun getUnPushedLocationDataByType(type: String): List<LocationEntity>

    //TODO: use this isPushed filter
    @Query("SELECT * FROM location_table WHERE type = :type AND timestamp BETWEEN :startOfDay AND :endOfDay")
    suspend fun getPreviousDayLocationDataByType(type: String, startOfDay: Long, endOfDay: Long): List<LocationEntity>

    /** ==================== FENCING ================ */

    @Insert
    suspend fun insertFenceData(geoJson: GeoJSONEntity)

    @Query("SELECT * FROM fence_table")
    suspend fun getFenceData(): List<GeoJSONEntity>

    @Query("SELECT * FROM fence_table WHERE boundaryId = :boundaryId")
    suspend fun getFenceDataByBoundaryId(boundaryId: String): List<GeoJSONEntity>

    @Query("SELECT * FROM fence_table WHERE fenceId = :fenceId")
    suspend fun getFenceDataByFenceId(fenceId: String): GeoJSONEntity

    @Query("DELETE FROM fence_table")
    suspend fun deleteFenceData()

    @Query("DELETE FROM fence_table WHERE boundaryId = :boundaryId")
    suspend fun deleteFenceDataByBoundaryId(boundaryId: String)

}