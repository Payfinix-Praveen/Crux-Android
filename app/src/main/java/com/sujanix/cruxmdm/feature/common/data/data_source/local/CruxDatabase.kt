package com.sujanix.cruxmdm.feature.common.data.data_source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationEntity
import com.sujanix.cruxmdm.feature.common.data.data_source.local.entity.User
import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationDao

@Database(
    entities = [User::class, ApplicationEntity::class],
//    entities = [User::class],
    version = 1
)
abstract class CruxDatabase: RoomDatabase() {

    abstract val cruxDao: CruxDao
    abstract val applicationDao: ApplicationDao

    companion object {
        const val DATABASE_NAME = "crux_mdm.db"
    }
}