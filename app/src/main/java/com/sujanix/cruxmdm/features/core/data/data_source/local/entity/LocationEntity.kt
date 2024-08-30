package com.sujanix.cruxmdm.features.core.data.data_source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sujanix.cruxmdm.features.core.data.model.request.Property

@Entity(tableName = "location_table")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var isPushed: Boolean = false,
    var type: String? = null,
    @ColumnInfo(name = "geo_coding") var geoCoding: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    var timestamp: String? = null
) {
    companion object {
        fun LocationEntity.toProperty(): Property {
            return Property(
                geo_coding = geoCoding!!,
                lat = lat!!,
                long = lon!!,
                timestamp = timestamp!!
            )
        }
    }
}