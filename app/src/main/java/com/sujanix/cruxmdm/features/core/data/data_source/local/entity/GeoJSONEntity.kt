package com.sujanix.cruxmdm.features.core.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sujanix.cruxmdm.features.core.data.data_source.local.Converters
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates

@Entity(tableName = "fence_table")
@TypeConverters(Converters::class)
data class GeoJSONEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fenceId: String,
    val fenceType: String,
    val boundaryId: String,
    val coordinates: Coordinates,
    val coordinateType: String
)

data class GeoJSON(
    val fenceId: String,
    val fenceType: String,
    val boundaryId: String,
    val coordinates: Coordinates,
    val coordinateType: String
)

fun GeoJSONEntity.toGeoJSON(): GeoJSON {
    return GeoJSON(
        fenceId = fenceId,
        fenceType = fenceType,
        boundaryId = boundaryId,
        coordinates = coordinates,
        coordinateType = coordinateType
    )
}

fun GeoJSON.toGeoJSONEntity(): GeoJSONEntity {
    return GeoJSONEntity(
        fenceId = fenceId,
        fenceType = fenceType,
        boundaryId = boundaryId,
        coordinates = coordinates,
        coordinateType = coordinateType
    )
}
