package com.sujanix.cruxmdm.data.data_source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sujanix.cruxmdm.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.data.data_source.local.entity.User
import com.sujanix.cruxmdm.data.model.Application

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