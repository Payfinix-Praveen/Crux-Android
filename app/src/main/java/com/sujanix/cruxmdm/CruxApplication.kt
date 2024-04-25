package com.sujanix.cruxmdm

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.sujanix.cruxmdm.util.Constant.APP_THEME
import com.sujanix.cruxmdm.util.Constant.DARK
import com.sujanix.cruxmdm.util.Constant.LIGHT
import com.sujanix.cruxmdm.util.Constant.PREFERENCE_NAME
import com.sujanix.cruxmdm.util.Constant.SYSTEM_DEFAULT
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CruxApplication: Application(), Configuration.Provider {

    private lateinit var preferences: SharedPreferences
    @Inject
    lateinit var workerFactory : HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        setAppTheme()
    }

    private fun setAppTheme(){
        preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val theme = preferences.getString(APP_THEME, SYSTEM_DEFAULT)
        when(theme) {
            SYSTEM_DEFAULT -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
            DARK -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
            LIGHT -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}