package com.sujanix.cruxmdm.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.ContentManagementDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.CruxDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.CruxDatabase
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesCruxApi(): CruxApi {
        return Retrofit.Builder()
            .baseUrl(CruxApi.BASE_URL)
            .client(
                OkHttpClient.Builder().also { client ->
                    val logging = HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                    client.connectTimeout(3, TimeUnit.MINUTES)
                    client.writeTimeout(3, TimeUnit.MINUTES)
                    client.readTimeout(3, TimeUnit.MINUTES)
                }.build()
            )
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun providesLocationApi(): LocationApi {
        return Retrofit.Builder()
            .baseUrl(LocationApi.LOCATION_BASE_URL)
            .client(
                OkHttpClient.Builder().also { client ->
                    val logging = HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                    client.connectTimeout(3, TimeUnit.MINUTES)
                    client.writeTimeout(3, TimeUnit.MINUTES)
                    client.readTimeout(3, TimeUnit.MINUTES)
                }.build()
            )
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun providesCruxDatabase(
        @ApplicationContext appContext: Context
    ): CruxDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            CruxDatabase::class.java,
            CruxDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providesCruxDao(
        db: CruxDatabase
    ): CruxDao {
        return db.cruxDao
    }

    @Provides
    @Singleton
    fun providesApplicationDao(
        db: CruxDatabase
    ): ApplicationDao {
        return db.applicationDao
    }

    @Provides
    @Singleton
    fun providesLocationDataDao(
        db: CruxDatabase
    ): LocationDataDao {
        return db.locationDao
    }

    @Provides
    @Singleton
    fun providesContentManagementDao(
        db: CruxDatabase
    ): ContentManagementDao {
        return db.contentManagementDao
    }
}