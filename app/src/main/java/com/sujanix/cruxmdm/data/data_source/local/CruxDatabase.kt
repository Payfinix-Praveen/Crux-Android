package com.sujanix.cruxmdm.data.data_source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sujanix.cruxmdm.data.model.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class CruxDatabase: RoomDatabase() {

    abstract val cruxDao: CruxDao

    companion object {
        const val DATABASE_NAME = "crux_db"
    }
}