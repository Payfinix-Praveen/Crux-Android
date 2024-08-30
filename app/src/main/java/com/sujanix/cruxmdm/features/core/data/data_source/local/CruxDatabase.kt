package com.sujanix.cruxmdm.features.core.data.data_source.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.User
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.RequestedAppEntity
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.ContentManagementDao
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.FileDataEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.CruxDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSONEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.LocationEntity

@Database(
    entities = [
        User::class,
        ApplicationEntity::class,
        LocationEntity::class,
        GeoJSONEntity::class,
        FileDataEntity::class,
        RequestedAppEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class CruxDatabase: RoomDatabase() {

    abstract val cruxDao: CruxDao
    abstract val applicationDao: ApplicationDao
    abstract val locationDao: LocationDataDao
    abstract val contentManagementDao: ContentManagementDao

    companion object {
        const val DATABASE_NAME = "crux_mdm.db"
    }
}