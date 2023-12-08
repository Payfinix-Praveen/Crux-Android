package com.sujanix.cruxmdm.di

import android.app.Application
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.sujanix.cruxmdm.data.data_source.local.CruxDatabase
import com.sujanix.cruxmdm.data.data_source.remote.CruxApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun providesCruxDatabase(
        appContext: Application
    ): CruxDatabase {
        return Room.databaseBuilder(
            appContext,
            CruxDatabase::class.java,
            CruxDatabase.DATABASE_NAME
        ).build()
    }
}